package com.sonddd.paywallet.repository;

import com.sonddd.paywallet.entity.Transaction;
import com.sonddd.paywallet.enums.TransactionStatus;
import com.sonddd.paywallet.enums.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

/**
 * Các điều kiện lọc giao dịch dùng JPA Criteria API (Specification).
 *
 * <p>Mỗi phương thức trả về {@code null} khi tham số lọc không có giá trị;
 * {@link Specification#and} bỏ qua điều kiện null. Nhờ vậy câu SQL chỉ chứa
 * những điều kiện thực sự được dùng, và không bao giờ gửi tham số null
 * xuống CSDL (PostgreSQL không suy ra được kiểu của "? IS NULL").</p>
 */
public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    /** Giao dịch mà ví là bên gửi HOẶC bên nhận. */
    public static Specification<Transaction> involvesWallet(Long walletId) {
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("fromWallet").get("id"), walletId),
                cb.equal(root.get("toWallet").get("id"), walletId));
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        return type == null ? null : (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> hasStatus(TransactionStatus status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Transaction> createdFrom(Instant from) {
        return from == null ? null
                : (root, query, cb) -> cb.greaterThanOrEqualTo(root.<Instant>get("createdAt"), from);
    }

    public static Specification<Transaction> createdTo(Instant to) {
        return to == null ? null
                : (root, query, cb) -> cb.lessThanOrEqualTo(root.<Instant>get("createdAt"), to);
    }
}
