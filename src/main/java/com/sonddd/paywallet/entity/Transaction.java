package com.sonddd.paywallet.entity;

import com.sonddd.paywallet.enums.TransactionStatus;
import com.sonddd.paywallet.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Bản ghi một giao dịch (nạp tiền / chuyển tiền / thanh toán).
 *
 * <p>Thiết kế thể hiện nhiều quan hệ ORM:
 * <ul>
 *   <li>{@code fromWallet}, {@code toWallet}: ManyToOne tới {@link Wallet} (có thể null tùy loại giao dịch).</li>
 *   <li>{@code merchant}: ManyToOne tới {@link Merchant} (chỉ dùng cho PAYMENT).</li>
 * </ul>
 * {@code reference} là mã giao dịch duy nhất. {@code idempotencyKey} chống thực hiện
 * trùng lặp cùng một yêu cầu (quan trọng với hệ thống thanh toán).</p>
 */
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_reference", columnList = "reference", unique = true),
        @Index(name = "idx_tx_idempotency", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_tx_from_wallet", columnList = "from_wallet_id"),
        @Index(name = "idx_tx_to_wallet", columnList = "to_wallet_id"),
        @Index(name = "idx_tx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String reference;

    @Column(name = "idempotency_key", unique = true, length = 80)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_wallet_id")
    private Wallet fromWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet_id")
    private Wallet toWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    @Column(length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
