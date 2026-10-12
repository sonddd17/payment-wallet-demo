package com.sonddd.paywallet.dto.response;

import com.sonddd.paywallet.enums.TransactionType;

import java.math.BigDecimal;

/**
 * Projection (interface-based) cho kết quả truy vấn GROUP BY trong báo cáo.
 * Spring Data JPA tự ánh xạ các alias của câu JPQL vào các getter này.
 */
public interface TransactionTypeStat {
    TransactionType getType();

    long getCount();

    BigDecimal getTotalAmount();
}
