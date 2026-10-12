package com.sonddd.paywallet.dto.response;

import com.sonddd.paywallet.entity.Wallet;

import java.math.BigDecimal;

/** Thông tin ví trả về cho client. */
public record WalletResponse(
        Long id,
        String username,
        BigDecimal balance,
        String currency,
        String status
) {
    public static WalletResponse from(Wallet w) {
        return new WalletResponse(
                w.getId(),
                w.getUser().getUsername(),
                w.getBalance(),
                w.getCurrency(),
                w.getStatus().name()
        );
    }
}
