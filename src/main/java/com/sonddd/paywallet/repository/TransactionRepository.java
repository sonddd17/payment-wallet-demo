package com.sonddd.paywallet.repository;

import com.sonddd.paywallet.dto.response.TransactionTypeStat;
import com.sonddd.paywallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Kế thừa {@link JpaSpecificationExecutor} để lọc động bằng
 * {@link TransactionSpecifications} (lịch sử giao dịch có nhiều bộ lọc tùy chọn).
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByReference(String reference);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Thống kê tổng hợp theo loại giao dịch (chỉ tính giao dịch thành công).
     * Dùng cho báo cáo của admin — ví dụ về truy vấn GROUP BY + hàm tổng hợp.
     */
    @Query("""
            SELECT t.type AS type, COUNT(t) AS count, COALESCE(SUM(t.amount), 0) AS totalAmount
            FROM Transaction t
            WHERE t.status = com.sonddd.paywallet.enums.TransactionStatus.SUCCESS
            GROUP BY t.type
            ORDER BY t.type
            """)
    List<TransactionTypeStat> summarizeByType();
}
