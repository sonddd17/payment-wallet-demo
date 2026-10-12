package com.sonddd.paywallet.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Dữ liệu đăng nhập. */
public record LoginRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        String username,

        @NotBlank(message = "Mật khẩu không được để trống")
        String password
) {
}
