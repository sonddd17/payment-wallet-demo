package com.sonddd.paywallet.entity;

import com.sonddd.paywallet.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Người dùng hệ thống. Mỗi User sở hữu đúng một {@link Wallet} (quan hệ 1-1).
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true),
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    /** Mật khẩu đã được băm bằng BCrypt (không bao giờ lưu plaintext). */
    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", length = 120)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean enabled;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Quan hệ 1-1 với ví. Ví được tạo cùng lúc với user khi đăng ký. */
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Wallet wallet;
}
