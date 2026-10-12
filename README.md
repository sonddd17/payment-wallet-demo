# PayWallet — Hệ thống Ví điện tử & Thanh toán (Demo)

Ứng dụng backend **mô phỏng** một ví điện tử: đăng ký/đăng nhập, nạp tiền, chuyển tiền,
thanh toán cho merchant, xem lịch sử và báo cáo. Dữ liệu là **tiền ảo**, không kết nối
cổng thanh toán thật — mục đích làm demo phỏng vấn vị trí Backend Java/Spring.

> ⚠️ Đây là dự án học tập/demo. Không dùng cho giao dịch tiền thật.

## 🌐 Demo trực tuyến

| | Link |
|---|---|
| Web demo | **https://sonddd.site** |
| Tài liệu API (Swagger) | **https://sonddd.site/swagger-ui.html** |
| Health check | https://sonddd.site/actuator/health |

Đăng nhập bằng `alice / alice123` (USER) hoặc `admin / admin123` (ADMIN).

### Kiến trúc triển khai

```
Trình duyệt ──HTTPS──▶ Cloudflare ──HTTPS (Full strict)──▶ Nginx :443 ──▶ Spring Boot :8080 ──▶ PostgreSQL 16
                                                           (VPS AlmaLinux 8)  (Docker)            (Docker, volume)
```

- **Mã hóa đầu–cuối:** Cloudflare ở chế độ *Full (strict)*, origin dùng chứng chỉ **Let's Encrypt**
  (certbot, phương thức webroot, tự gia hạn).
- **Chỉ nhận kết nối từ Cloudflare:** Nginx chặn mọi IP ngoài dải IP chính thức của Cloudflare,
  nên không thể gọi thẳng vào IP server để bỏ qua Cloudflare. Log và ứng dụng thấy IP thật của khách
  qua header `CF-Connecting-IP`.
- **Ứng dụng không mở ra ngoài:** cổng 8080 chỉ bind `127.0.0.1`; mọi truy cập đi qua Nginx.
- **Bí mật** (mật khẩu DB, JWT secret) sinh ngẫu nhiên, lưu trong `deploy/.env` trên server, không commit.
- VPS 1 CPU / 1GB RAM (+2GB swap): jar build sẵn, image chỉ chứa JRE, heap giới hạn 384MB.

## Công nghệ & kỹ năng thể hiện

| Nhóm | Chi tiết |
|------|----------|
| Ngôn ngữ / nền tảng | Java 21, Spring Boot 3.4 |
| Web / API | Spring MVC, REST API, chuẩn hóa response, validation |
| ORM | Spring Data JPA, Hibernate (quan hệ 1-1, 1-N, N-1) |
| CSDL | H2 (mặc định), PostgreSQL (profile `postgres`); lọc động bằng JPA Specification, GROUP BY + SUM, index |
| Bảo mật | Spring Security 6, JWT, BCrypt, phân quyền USER/ADMIN |
| Giao dịch | `@Transactional`, khóa bi quan (`PESSIMISTIC_WRITE`), khóa lạc quan (`@Version`), idempotency |
| Kiểm thử | JUnit 5, Mockito (unit), Spring Boot Test + MockMvc (integration) |
| Build / Deploy | Maven, Docker (multi-stage), Docker Compose, Jenkins (Jenkinsfile) |
| Hạ tầng | VPS Linux, Nginx reverse proxy, Let's Encrypt, Cloudflare (Full strict), giới hạn truy cập theo IP |
| Khác | Swagger/OpenAPI, Actuator health check, xử lý ngoại lệ tập trung (không lộ lỗi nội bộ), Git |

## Tài khoản demo (được tạo sẵn khi khởi động)

| Username | Mật khẩu | Vai trò | Số dư ban đầu |
|----------|----------|---------|---------------|
| `admin`  | `admin123` | ADMIN | 0 |
| `alice`  | `alice123` | USER  | 750.000 |
| `bob`    | `bob123`   | USER  | 500.000 |

Merchant demo: `COFFEE`, `SHOP`.

## Cách chạy

### Cách 1 — Maven + H2 (nhanh nhất, chỉ cần JDK + Maven)

```bash
mvn spring-boot:run
```

Rồi mở: <http://localhost:8080> (giao diện demo) · <http://localhost:8080/swagger-ui.html> (API).

### Cách 2 — Docker Compose (app + PostgreSQL, giống production)

```bash
docker compose up --build
```

### Triển khai lên VPS (production)

Mọi cấu hình nằm trong thư mục [`deploy/`](deploy/):

| File | Vai trò |
|------|---------|
| [`Dockerfile`](deploy/Dockerfile) | Image chạy jar build sẵn trên JRE 21 (không build Maven trên server ít RAM) |
| [`docker-compose.prod.yml`](deploy/docker-compose.prod.yml) | App + PostgreSQL 16; app chỉ mở cho `127.0.0.1`, giới hạn heap |
| [`nginx-paywallet.conf`](deploy/nginx-paywallet.conf) | Reverse proxy HTTPS :443, cổng 80 chỉ phục vụ xác thực Let's Encrypt và chuyển hướng sang HTTPS |
| [`cloudflare-ips.sh`](deploy/cloudflare-ips.sh) | Sinh danh sách IP Cloudflare cho Nginx (chặn IP khác + lấy IP thật của khách) |

**Chạy ứng dụng** (trên server, trong `/opt/paywallet/deploy`, đã có `payment-wallet-demo.jar` và `.env`):

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

**Nginx + HTTPS** (làm một lần):

```bash
bash /opt/paywallet/deploy/cloudflare-ips.sh
certbot certonly --webroot -w /var/www/letsencrypt -d sonddd.site -d www.sonddd.site \
  --deploy-hook "systemctl reload nginx"
cp /opt/paywallet/deploy/nginx-paywallet.conf /etc/nginx/conf.d/paywallet.conf
nginx -t && systemctl reload nginx
```

Sau đó đặt Cloudflare **SSL/TLS → Full (strict)**. Chứng chỉ tự gia hạn qua `certbot-renew.timer`.

**Cập nhật phiên bản mới:** build jar trên máy (`mvn package`), copy jar lên `/opt/paywallet/deploy/`,
rồi chạy `docker compose -f docker-compose.prod.yml up -d --build app`.

## Các endpoint chính

| Method | Đường dẫn | Mô tả | Quyền |
|--------|-----------|-------|-------|
| POST | `/api/auth/register` | Đăng ký + tạo ví | Công khai |
| POST | `/api/auth/login` | Đăng nhập, nhận JWT | Công khai |
| GET  | `/api/wallet/me` | Xem số dư | USER |
| POST | `/api/wallet/topup` | Nạp tiền | USER |
| POST | `/api/payments/transfer` | Chuyển tiền | USER |
| POST | `/api/payments/pay` | Thanh toán merchant | USER |
| GET  | `/api/payments/history` | Lịch sử (lọc + phân trang) | USER |
| GET  | `/api/merchants` | Danh sách merchant | Công khai |
| GET  | `/api/admin/transactions` | Toàn bộ giao dịch | ADMIN |
| GET  | `/api/admin/report` | Báo cáo tổng hợp | ADMIN |

## Kiểm thử

```bash
mvn test
```

- **Unit test** ([`PaymentServiceImplTest`](src/test/java/com/sonddd/paywallet/service/PaymentServiceImplTest.java)):
  Mockito giả lập repository — chuyển tiền thành công, thiếu số dư, tự chuyển cho mình, idempotency.
- **Integration test** ([`AuthFlowIntegrationTest`](src/test/java/com/sonddd/paywallet/controller/AuthFlowIntegrationTest.java)):
  khởi động toàn bộ Spring Boot + H2, gọi API qua MockMvc — đăng ký → đăng nhập → nạp tiền → số dư → lịch sử
  với các bộ lọc; gọi API khi chưa đăng nhập bị từ chối.

## Cấu trúc thư mục

```
├── deploy/            # Dockerfile, docker-compose, Nginx, script IP Cloudflare (production)
├── docs/              # PDF hướng dẫn demo
├── Dockerfile         # Build multi-stage (Maven -> JRE) để chạy local bằng Docker
├── docker-compose.yml # App + PostgreSQL cho môi trường local
├── Jenkinsfile        # Pipeline CI/CD: checkout -> test -> package -> docker build
└── src/main/java/com/sonddd/paywallet/
    ├── config/        # SecurityConfig, OpenApiConfig, DataSeeder
    ├── controller/    # REST controllers
    ├── dto/           # request & response (record)
    ├── entity/        # User, Wallet, Merchant, Transaction
    ├── enums/         # Role, TransactionType, TransactionStatus, WalletStatus
    ├── exception/     # Exception + GlobalExceptionHandler
    ├── repository/    # Spring Data JPA repositories + TransactionSpecifications (lọc động)
    ├── security/      # JWT: JwtService, JwtAuthenticationFilter, UserDetailsService
    ├── service/       # Interface nghiệp vụ + impl/
    └── util/          # References (sinh mã giao dịch)
```

Tác giả: **sonddd** · Demo phỏng vấn 2026.
