package com.sonddd.paywallet.dto.response;

/** Phản hồi sau khi đăng nhập thành công: chứa JWT access token. */
public record AuthResponse(
        String accessToken,
        String tokenType,
        String username,
        String role,
        long expiresInSeconds
) {
}
