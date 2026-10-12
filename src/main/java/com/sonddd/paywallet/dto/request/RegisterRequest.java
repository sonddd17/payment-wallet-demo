package com.sonddd.paywallet.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dữ liệu đăng ký tài khoản mới. */
public record RegisterRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự")
        String username,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 100, message = "Mật khẩu phải có ít nhất 6 ký tự")
        String password,

        @Size(max = 120, message = "Họ tên tối đa 120 ký tự")
        String fullName
) {
}
