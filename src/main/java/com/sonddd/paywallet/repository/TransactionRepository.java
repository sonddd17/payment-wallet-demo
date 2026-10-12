package com.sonddd.paywallet.repository;

import com.sonddd.paywallet.dto.response.TransactionTypeStat;
import com.sonddd.paywallet.entity.Transaction;
import com.sonddd.paywallet.enums.TransactionStatus;
import com.sonddd.paywallet.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByReference(String reference);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Lịch sử giao dịch của một ví (là bên gửi HOẶC bên nhận), có lọc tùy chọn
     * theo loại, trạng thái và khoảng thời gian. Các tham số null sẽ được bỏ qua.
     * Ví dụ về <b>truy vấn phức tạp</b> với nhiều điều kiện động + phân trang.
     */
    @Query("""
            SELECT t FROM Transaction t
            WHERE (t.fromWallet.id = :walletId OR t.toWallet.id = :walletId)
              AND (:type IS NULL OR t.type = :type)
              AND (:status IS NULL OR t.status = :status)
              AND (:fromDate IS NULL OR t.createdAt >= :fromDate)
              AND (:toDate IS NULL OR t.createdAt <= :toDate)
            """)
    Page<Transaction> searchForWallet(@Param("walletId") Long walletId,
                                      @Param("type") TransactionType type,
                                      @Param("status") TransactionStatus status,
                                      @Param("fromDate") Instant fromDate,
                                      @Param("toDate") Instant toDate,
                                      Pageable pageable);

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
