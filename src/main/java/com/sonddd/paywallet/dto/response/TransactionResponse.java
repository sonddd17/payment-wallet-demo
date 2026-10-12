package com.sonddd.paywallet.dto.response;

import com.sonddd.paywallet.entity.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

/** Thông tin giao dịch trả về cho client (đã phẳng hóa các quan hệ). */
public record TransactionResponse(
        Long id,
        String reference,
        String type,
        String status,
        BigDecimal amount,
        BigDecimal fee,
        String fromUsername,
        String toUsername,
        String merchantCode,
        String description,
        Instant createdAt,
        Instant completedAt
) {
    public static TransactionResponse from(Transaction t) {
        String fromUser = t.getFromWallet() != null ? t.getFromWallet().getUser().getUsername() : null;
        String toUser = t.getToWallet() != null ? t.getToWallet().getUser().getUsername() : null;
        String merchantCode = t.getMerchant() != null ? t.getMerchant().getCode() : null;
        return new TransactionResponse(
                t.getId(),
                t.getReference(),
                t.getType().name(),
                t.getStatus().name(),
                t.getAmount(),
                t.getFee(),
                fromUser,
                toUser,
                merchantCode,
                t.getDescription(),
                t.getCreatedAt(),
                t.getCompletedAt()
        );
    }
}
