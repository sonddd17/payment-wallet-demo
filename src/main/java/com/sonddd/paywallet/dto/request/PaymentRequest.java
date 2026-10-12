package com.sonddd.paywallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Yêu cầu thanh toán cho một merchant theo mã merchant. */
public record PaymentRequest(
        @NotBlank(message = "Mã merchant không được để trống")
        String merchantCode,

        @NotNull(message = "Số tiền không được để trống")
        @DecimalMin(value = "0.01", message = "Số tiền thanh toán phải lớn hơn 0")
        @Digits(integer = 17, fraction = 2, message = "Số tiền không hợp lệ")
        BigDecimal amount,

        String description,

        String idempotencyKey
) {
}
