package com.sonddd.paywallet.enums;

/** Loại giao dịch trong hệ thống. */
public enum TransactionType {
    /** Nạp tiền vào ví (mô phỏng). */
    TOPUP,
    /** Chuyển tiền giữa hai ví người dùng. */
    TRANSFER,
    /** Thanh toán cho một merchant (người bán). */
    PAYMENT
}
