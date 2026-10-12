package com.sonddd.paywallet.exception;

/** Số dư ví không đủ để thực hiện giao dịch -> HTTP 422. */
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
