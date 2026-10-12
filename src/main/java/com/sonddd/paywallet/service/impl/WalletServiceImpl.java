package com.sonddd.paywallet.service.impl;

import com.sonddd.paywallet.dto.request.TopUpRequest;
import com.sonddd.paywallet.dto.response.TransactionResponse;
import com.sonddd.paywallet.dto.response.WalletResponse;
import com.sonddd.paywallet.entity.Transaction;
import com.sonddd.paywallet.entity.Wallet;
import com.sonddd.paywallet.enums.TransactionStatus;
import com.sonddd.paywallet.enums.TransactionType;
import com.sonddd.paywallet.enums.WalletStatus;
import com.sonddd.paywallet.exception.BusinessException;
import com.sonddd.paywallet.exception.ResourceNotFoundException;
import com.sonddd.paywallet.repository.TransactionRepository;
import com.sonddd.paywallet.repository.WalletRepository;
import com.sonddd.paywallet.service.WalletService;
import com.sonddd.paywallet.util.References;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletServiceImpl(WalletRepository walletRepository,
                             TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getMyWallet(String username) {
        Wallet wallet = walletRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví của người dùng"));
        return WalletResponse.from(wallet);
    }

    @Override
    @Transactional
    public TransactionResponse topUp(String username, TopUpRequest request) {
        // Chống thực hiện trùng lặp: nếu idempotencyKey đã dùng, trả lại giao dịch cũ.
        if (StringUtils.hasText(request.idempotencyKey())) {
            var existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
            if (existing.isPresent()) {
                return TransactionResponse.from(existing.get());
            }
        }

        Long walletId = walletRepository.findIdByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví của người dùng"));

        // Khóa rồi mới đọc số dư, để cập nhật an toàn khi có truy cập đồng thời.
        Wallet locked = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví"));

        if (locked.getStatus() != WalletStatus.ACTIVE) {
            throw new BusinessException("Ví đang bị khóa, không thể nạp tiền");
        }

        BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
        locked.setBalance(locked.getBalance().add(amount));

        Transaction tx = Transaction.builder()
                .reference(References.newTransactionRef())
                .idempotencyKey(StringUtils.hasText(request.idempotencyKey()) ? request.idempotencyKey() : null)
                .type(TransactionType.TOPUP)
                .status(TransactionStatus.SUCCESS)
                .amount(amount)
                .fee(BigDecimal.ZERO.setScale(2))
                .toWallet(locked)
                .description(StringUtils.hasText(request.description()) ? request.description() : "Nạp tiền vào ví")
                .completedAt(Instant.now())
                .build();

        tx = transactionRepository.save(tx);
        return TransactionResponse.from(tx);
    }
}
