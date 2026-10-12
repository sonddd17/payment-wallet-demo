package com.sonddd.paywallet.entity;

import com.sonddd.paywallet.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Ví điện tử của người dùng.
 *
 * <p>Số dư dùng {@link BigDecimal} để tránh sai số dấu phẩy động khi tính tiền.
 * Trường {@code version} phục vụ <b>khóa lạc quan (optimistic locking)</b> của JPA:
 * nếu hai giao dịch cùng sửa một ví đồng thời, giao dịch tới sau sẽ nhận
 * {@code OptimisticLockException} thay vì ghi đè mất dữ liệu.</p>
 */
@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletStatus status;

    /** Số phiên bản dùng cho khóa lạc quan (JPA @Version). */
    @Version
    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
