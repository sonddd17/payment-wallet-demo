package com.sonddd.paywallet.service;

import com.sonddd.paywallet.dto.request.LoginRequest;
import com.sonddd.paywallet.dto.request.RegisterRequest;
import com.sonddd.paywallet.dto.response.AuthResponse;
import com.sonddd.paywallet.dto.response.WalletResponse;

public interface AuthService {

    /** Đăng ký user mới + tạo ví số dư 0. */
    WalletResponse register(RegisterRequest request);

    /** Đăng nhập, trả về JWT. */
    AuthResponse login(LoginRequest request);
}
