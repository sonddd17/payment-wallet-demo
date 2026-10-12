package com.sonddd.paywallet.service;

import com.sonddd.paywallet.dto.request.TopUpRequest;
import com.sonddd.paywallet.dto.response.TransactionResponse;
import com.sonddd.paywallet.dto.response.WalletResponse;

public interface WalletService {

    WalletResponse getMyWallet(String username);

    /** Nạp tiền vào ví của chính mình (mô phỏng). */
    TransactionResponse topUp(String username, TopUpRequest request);
}
