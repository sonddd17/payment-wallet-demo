package com.sonddd.paywallet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Merchant (người bán / điểm chấp nhận thanh toán).
 * Giữ một số dư quyết toán riêng; mỗi lần có khách thanh toán thành công,
 * số dư merchant tăng lên tương ứng.
 */
@Entity
@Table(name = "merchants", indexes = {
        @Index(name = "idx_merchants_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private boolean active;

    @Version
    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
