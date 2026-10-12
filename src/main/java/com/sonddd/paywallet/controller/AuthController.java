package com.sonddd.paywallet.controller;

import com.sonddd.paywallet.dto.request.LoginRequest;
import com.sonddd.paywallet.dto.request.RegisterRequest;
import com.sonddd.paywallet.dto.response.ApiResponse;
import com.sonddd.paywallet.dto.response.AuthResponse;
import com.sonddd.paywallet.dto.response.WalletResponse;
import com.sonddd.paywallet.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "1. Xác thực", description = "Đăng ký và đăng nhập")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Đăng ký tài khoản mới (kèm tạo ví)")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<WalletResponse>> register(@Valid @RequestBody RegisterRequest request) {
        WalletResponse wallet = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đăng ký thành công", wallet));
    }

    @Operation(summary = "Đăng nhập, nhận JWT access token")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Đăng nhập thành công", authService.login(request));
    }
}
