package com.sonddd.paywallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Yêu cầu chuyển tiền tới ví của người dùng khác. */
public record TransferRequest(
        @NotBlank(message = "Người nhận không được để trống")
        String toUsername,

        @NotNull(message = "Số tiền không được để trống")
        @DecimalMin(value = "0.01", message = "Số tiền chuyển phải lớn hơn 0")
        @Digits(integer = 17, fraction = 2, message = "Số tiền không hợp lệ")
        BigDecimal amount,

        String description,

        String idempotencyKey
) {
}
