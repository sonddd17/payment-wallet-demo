package com.sonddd.paywallet.exception;

/** Lỗi nghiệp vụ chung (vi phạm quy tắc nghiệp vụ) -> HTTP 400. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
