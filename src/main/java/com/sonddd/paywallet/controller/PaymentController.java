package com.sonddd.paywallet.controller;

import com.sonddd.paywallet.dto.request.PaymentRequest;
import com.sonddd.paywallet.dto.request.TransferRequest;
import com.sonddd.paywallet.dto.response.ApiResponse;
import com.sonddd.paywallet.dto.response.PageResponse;
import com.sonddd.paywallet.dto.response.TransactionResponse;
import com.sonddd.paywallet.enums.TransactionStatus;
import com.sonddd.paywallet.enums.TransactionType;
import com.sonddd.paywallet.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Tag(name = "3. Thanh toán", description = "Chuyển tiền, thanh toán merchant và lịch sử giao dịch")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Chuyển tiền tới ví người dùng khác")
    @PostMapping("/transfer")
    public ApiResponse<TransactionResponse> transfer(Authentication authentication,
                                                     @Valid @RequestBody TransferRequest request) {
        return ApiResponse.ok("Chuyển tiền thành công",
                paymentService.transfer(authentication.getName(), request));
    }

    @Operation(summary = "Thanh toán cho merchant")
    @PostMapping("/pay")
    public ApiResponse<TransactionResponse> pay(Authentication authentication,
                                                @Valid @RequestBody PaymentRequest request) {
        return ApiResponse.ok("Thanh toán thành công",
                paymentService.pay(authentication.getName(), request));
    }

    @Operation(summary = "Lịch sử giao dịch của tôi (lọc + phân trang)")
    @GetMapping("/history")
    public ApiResponse<PageResponse<TransactionResponse>> history(
            Authentication authentication,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<TransactionResponse> result = paymentService.history(
                authentication.getName(), type, status, fromDate, toDate, pageable);
        return ApiResponse.ok(result);
    }
}
