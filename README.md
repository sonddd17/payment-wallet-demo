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

Hạ tầng: VPS AlmaLinux 8 · Docker Compose (Spring Boot + PostgreSQL 16) · Nginx reverse proxy · Cloudflare (HTTPS).

## Công nghệ & kỹ năng thể hiện

| Nhóm | Chi tiết |
|------|----------|
| Ngôn ngữ / nền tảng | Java 21, Spring Boot 3.4 |
| Web / API | Spring MVC, REST API, chuẩn hóa response, validation |
| ORM | Spring Data JPA, Hibernate (quan hệ 1-1, 1-N, N-1) |
| CSDL | H2 (mặc định), PostgreSQL (profile `postgres`); truy vấn JPQL phức tạp, GROUP BY, index |
| Bảo mật | Spring Security 6, JWT, BCrypt, phân quyền USER/ADMIN |
| Giao dịch | `@Transactional`, khóa bi quan (`PESSIMISTIC_WRITE`), khóa lạc quan (`@Version`), idempotency |
| Kiểm thử | JUnit 5, Mockito (unit), Spring Boot Test + MockMvc (integration) |
| Build / Deploy | Maven, Docker (multi-stage), Docker Compose, Jenkins (Jenkinsfile) |
| Khác | Swagger/OpenAPI, xử lý ngoại lệ tập trung, Git |

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

Cấu hình nằm trong thư mục [`deploy/`](deploy/): `Dockerfile` chạy jar đã build sẵn trên image JRE,
`docker-compose.prod.yml` (app + PostgreSQL, app chỉ mở cho localhost) và cấu hình Nginx.
Bí mật (`DB_PASSWORD`, `JWT_SECRET`) đặt trong file `deploy/.env` trên server, không commit.

```bash
# Trên server, trong /opt/paywallet/deploy (đã copy payment-wallet-demo.jar vào đây)
docker compose -f docker-compose.prod.yml up -d --build
```

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

## Cấu trúc thư mục

```
src/main/java/com/sonddd/paywallet/
├── config/        # SecurityConfig, OpenApiConfig, DataSeeder
├── controller/    # REST controllers
├── dto/           # request & response (record)
├── entity/        # User, Wallet, Merchant, Transaction
├── enums/         # Role, TransactionType, TransactionStatus, WalletStatus
├── exception/     # Exception + GlobalExceptionHandler
├── repository/    # Spring Data JPA repositories
├── security/      # JWT: JwtService, JwtAuthenticationFilter, UserDetailsService
├── service/       # Interface nghiệp vụ + impl/
└── util/          # References (sinh mã giao dịch)
```

Tác giả: **sonddd** · Demo phỏng vấn 2026.
