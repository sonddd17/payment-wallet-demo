package com.sonddd.paywallet.repository;

import com.sonddd.paywallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser_Username(String username);

    Optional<Wallet> findByUser_Id(Long userId);

    /**
     * Chỉ lấy id ví, KHÔNG nạp entity vào persistence context.
     * Dùng trước {@link #findByIdForUpdate}: nếu entity đã được nạp sẵn (đọc không khóa),
     * câu lệnh khóa sẽ trả về đúng object cũ mà không làm mới số dư -> tính trên số dư cũ.
     */
    @Query("SELECT w.id FROM Wallet w WHERE w.user.username = :username")
    Optional<Long> findIdByUsername(@Param("username") String username);

    /**
     * Lấy ví kèm <b>khóa ghi bi quan (pessimistic write lock)</b>.
     * Dùng trong luồng chuyển tiền/thanh toán để đảm bảo chỉ một giao dịch
     * được cập nhật số dư của ví tại một thời điểm, tránh race condition.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") Long id);
}
