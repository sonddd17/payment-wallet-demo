package com.sonddd.paywallet.service;

import com.sonddd.paywallet.dto.response.PageResponse;
import com.sonddd.paywallet.dto.response.TransactionResponse;
import com.sonddd.paywallet.dto.response.TransactionTypeStat;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {

    PageResponse<TransactionResponse> listAllTransactions(Pageable pageable);

    /** Báo cáo tổng hợp theo loại giao dịch (chỉ giao dịch thành công). */
    List<TransactionTypeStat> report();
}
