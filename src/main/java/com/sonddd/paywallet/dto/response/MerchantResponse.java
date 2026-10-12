package com.sonddd.paywallet.dto.response;

import com.sonddd.paywallet.entity.Merchant;

/** Thông tin merchant công khai để client chọn khi thanh toán. */
public record MerchantResponse(String code, String name, boolean active) {
    public static MerchantResponse from(Merchant m) {
        return new MerchantResponse(m.getCode(), m.getName(), m.isActive());
    }
}
