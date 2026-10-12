package com.sonddd.paywallet.controller;

import com.sonddd.paywallet.dto.response.ApiResponse;
import com.sonddd.paywallet.dto.response.MerchantResponse;
import com.sonddd.paywallet.repository.MerchantRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "4. Merchant", description = "Danh sách merchant có thể thanh toán")
@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantRepository merchantRepository;

    public MerchantController(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Operation(summary = "Danh sách merchant (công khai)")
    @GetMapping
    public ApiResponse<List<MerchantResponse>> list() {
        List<MerchantResponse> merchants = merchantRepository.findAll(Sort.by("name")).stream()
                .map(MerchantResponse::from)
                .toList();
        return ApiResponse.ok(merchants);
    }
}
