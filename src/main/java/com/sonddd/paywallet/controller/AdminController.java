package com.sonddd.paywallet.controller;

import com.sonddd.paywallet.dto.response.ApiResponse;
import com.sonddd.paywallet.dto.response.PageResponse;
import com.sonddd.paywallet.dto.response.TransactionResponse;
import com.sonddd.paywallet.dto.response.TransactionTypeStat;
import com.sonddd.paywallet.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "5. Quản trị", description = "Chỉ dành cho ADMIN: xem toàn bộ giao dịch và báo cáo")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")   // phòng thủ nhiều lớp, bên cạnh cấu hình URL
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Xem toàn bộ giao dịch của hệ thống (phân trang)")
    @GetMapping("/transactions")
    public ApiResponse<PageResponse<TransactionResponse>> allTransactions(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(adminService.listAllTransactions(pageable));
    }

    @Operation(summary = "Báo cáo tổng hợp theo loại giao dịch")
    @GetMapping("/report")
    public ApiResponse<List<TransactionTypeStat>> report() {
        return ApiResponse.ok(adminService.report());
    }
}
