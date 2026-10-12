package com.sonddd.paywallet.controller;

import com.sonddd.paywallet.dto.request.TopUpRequest;
import com.sonddd.paywallet.dto.response.ApiResponse;
import com.sonddd.paywallet.dto.response.TransactionResponse;
import com.sonddd.paywallet.dto.response.WalletResponse;
import com.sonddd.paywallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "2. Ví", description = "Xem số dư và nạp tiền")
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(summary = "Xem ví của tôi (số dư hiện tại)")
    @GetMapping("/me")
    public ApiResponse<WalletResponse> myWallet(Authentication authentication) {
        return ApiResponse.ok(walletService.getMyWallet(authentication.getName()));
    }

    @Operation(summary = "Nạp tiền vào ví (mô phỏng)")
    @PostMapping("/topup")
    public ApiResponse<TransactionResponse> topUp(Authentication authentication,
                                                  @Valid @RequestBody TopUpRequest request) {
        return ApiResponse.ok("Nạp tiền thành công", walletService.topUp(authentication.getName(), request));
    }
}
