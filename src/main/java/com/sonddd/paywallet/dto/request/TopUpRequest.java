package com.sonddd.paywallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Yêu cầu nạp tiền vào ví của chính mình (mô phỏng). */
public record TopUpRequest(
        @NotNull(message = "Số tiền không được để trống")
        @DecimalMin(value = "0.01", message = "Số tiền nạp phải lớn hơn 0")
        @Digits(integer = 17, fraction = 2, message = "Số tiền không hợp lệ")
        BigDecimal amount,

        String description,

        /** Khóa chống trùng (tùy chọn): gửi lại cùng key sẽ không nạp 2 lần. */
        String idempotencyKey
) {
}
