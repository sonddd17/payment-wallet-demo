package com.sonddd.paywallet.exception;

/** Tài nguyên đã tồn tại (vd username/email trùng) -> HTTP 409. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
