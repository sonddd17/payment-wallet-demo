package com.sonddd.paywallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Điểm khởi động của ứng dụng PayWallet.
 *
 * <p>Hệ thống Ví điện tử &amp; Thanh toán (mô phỏng) demo các kỹ năng:
 * Spring Boot, Spring MVC, Spring Data JPA/Hibernate, Spring Security (JWT),
 * xử lý giao dịch (transaction) &amp; khóa lạc quan (optimistic lock),
 * unit test (JUnit + Mockito) và đóng gói Docker/Maven.</p>
 */
@SpringBootApplication
public class PayWalletApplication {

    public static void main(String[] args) {
        // Cho phép Byte Buddy (Hibernate dùng để sinh proxy) hoạt động trên JDK mới (vd JDK 25)
        // mà bản Byte Buddy đi kèm chưa chính thức hỗ trợ. Không ảnh hưởng gì khi chạy JDK 21.
        System.setProperty("net.bytebuddy.experimental", "true");
        SpringApplication.run(PayWalletApplication.class, args);
    }
}
