package com.sonddd.paywallet.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Vỏ bọc chuẩn cho mọi phản hồi API thành công: {success, message, data}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
}
