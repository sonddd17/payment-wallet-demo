package com.sonddd.paywallet.service;

import com.sonddd.paywallet.dto.request.PaymentRequest;
import com.sonddd.paywallet.dto.request.TransferRequest;
import com.sonddd.paywallet.dto.response.PageResponse;
import com.sonddd.paywallet.dto.response.TransactionResponse;
import com.sonddd.paywallet.enums.TransactionStatus;
import com.sonddd.paywallet.enums.TransactionType;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface PaymentService {

    /** Chuyển tiền tới ví người dùng khác. */
    TransactionResponse transfer(String senderUsername, TransferRequest request);

    /** Thanh toán cho merchant. */
    TransactionResponse pay(String payerUsername, PaymentRequest request);

    /** Lịch sử giao dịch của ví người dùng (có lọc + phân trang). */
    PageResponse<TransactionResponse> history(String username,
                                              TransactionType type,
                                              TransactionStatus status,
                                              Instant fromDate,
                                              Instant toDate,
                                              Pageable pageable);
}
