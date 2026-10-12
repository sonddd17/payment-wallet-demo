package com.sonddd.paywallet.service.impl;

import com.sonddd.paywallet.dto.request.PaymentRequest;
import com.sonddd.paywallet.dto.request.TransferRequest;
import com.sonddd.paywallet.dto.response.PageResponse;
import com.sonddd.paywallet.dto.response.TransactionResponse;
import com.sonddd.paywallet.entity.Merchant;
import com.sonddd.paywallet.entity.Transaction;
import com.sonddd.paywallet.entity.Wallet;
import com.sonddd.paywallet.enums.TransactionStatus;
import com.sonddd.paywallet.enums.TransactionType;
import com.sonddd.paywallet.enums.WalletStatus;
import com.sonddd.paywallet.exception.BusinessException;
import com.sonddd.paywallet.exception.InsufficientBalanceException;
import com.sonddd.paywallet.exception.ResourceNotFoundException;
import com.sonddd.paywallet.repository.MerchantRepository;
import com.sonddd.paywallet.repository.TransactionRepository;
import com.sonddd.paywallet.repository.TransactionSpecifications;
import com.sonddd.paywallet.repository.WalletRepository;
import com.sonddd.paywallet.service.PaymentService;
import com.sonddd.paywallet.util.References;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final WalletRepository walletRepository;
    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;

    public PaymentServiceImpl(WalletRepository walletRepository,
                              MerchantRepository merchantRepository,
                              TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TransactionResponse transfer(String senderUsername, TransferRequest request) {
        Optional<Transaction> duplicate = findByIdempotencyKey(request.idempotencyKey());
        if (duplicate.isPresent()) {
            return TransactionResponse.from(duplicate.get());
        }

        if (senderUsername.equalsIgnoreCase(request.toUsername())) {
            throw new BusinessException("Không thể chuyển tiền cho chính mình");
        }

        // Chỉ lấy id, chưa đọc số dư: số dư phải được đọc SAU khi đã khóa.
        Long senderId = walletRepository.findIdByUsername(senderUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví người gửi"));
        Long receiverId = walletRepository.findIdByUsername(request.toUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người nhận: " + request.toUsername()));
        if (senderId.equals(receiverId)) {
            throw new BusinessException("Không thể chuyển tiền cho chính mình");
        }

        // Khóa (SELECT ... FOR UPDATE) hai ví theo thứ tự id tăng dần để tránh deadlock
        // khi hai người chuyển chéo cho nhau. Giao dịch đến sau sẽ chờ ở đây, rồi đọc số dư mới nhất.
        Wallet first = walletRepository.findByIdForUpdate(Math.min(senderId, receiverId)).orElseThrow();
        Wallet second = walletRepository.findByIdForUpdate(Math.max(senderId, receiverId)).orElseThrow();
        Wallet sender = first.getId().equals(senderId) ? first : second;
        Wallet receiver = sender == first ? second : first;

        if (sender.getStatus() != WalletStatus.ACTIVE) {
            throw new BusinessException("Ví của bạn đang bị khóa");
        }
        if (receiver.getStatus() != WalletStatus.ACTIVE) {
            throw new BusinessException("Ví người nhận đang bị khóa");
        }

        BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = BigDecimal.ZERO.setScale(2);
        BigDecimal totalDebit = amount.add(fee);

        if (sender.getBalance().compareTo(totalDebit) < 0) {
            throw new InsufficientBalanceException("Số dư không đủ để thực hiện giao dịch");
        }

        sender.setBalance(sender.getBalance().subtract(totalDebit));
        receiver.setBalance(receiver.getBalance().add(amount));

        Transaction tx = Transaction.builder()
                .reference(References.newTransactionRef())
                .idempotencyKey(StringUtils.hasText(request.idempotencyKey()) ? request.idempotencyKey() : null)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .amount(amount)
                .fee(fee)
                .fromWallet(sender)
                .toWallet(receiver)
                .description(StringUtils.hasText(request.description())
                        ? request.description() : "Chuyển tiền tới " + receiver.getUser().getUsername())
                .completedAt(Instant.now())
                .build();

        return TransactionResponse.from(transactionRepository.save(tx));
    }

    @Override
    @Transactional
    public TransactionResponse pay(String payerUsername, PaymentRequest request) {
        Optional<Transaction> duplicate = findByIdempotencyKey(request.idempotencyKey());
        if (duplicate.isPresent()) {
            return TransactionResponse.from(duplicate.get());
        }

        Long walletId = walletRepository.findIdByUsername(payerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví người thanh toán"));
        Long merchantId = merchantRepository.findIdByCode(request.merchantCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy merchant: " + request.merchantCode()));

        // Khóa rồi mới đọc: luôn khóa ví trước, merchant sau (cùng một thứ tự -> không deadlock).
        Wallet wallet = walletRepository.findByIdForUpdate(walletId).orElseThrow();
        Merchant merchant = merchantRepository.findByIdForUpdate(merchantId).orElseThrow();

        if (!merchant.isActive()) {
            throw new BusinessException("Merchant đang ngừng nhận thanh toán");
        }

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new BusinessException("Ví của bạn đang bị khóa");
        }

        BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Số dư không đủ để thanh toán");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        merchant.setBalance(merchant.getBalance().add(amount));

        Transaction tx = Transaction.builder()
                .reference(References.newTransactionRef())
                .idempotencyKey(StringUtils.hasText(request.idempotencyKey()) ? request.idempotencyKey() : null)
                .type(TransactionType.PAYMENT)
                .status(TransactionStatus.SUCCESS)
                .amount(amount)
                .fee(BigDecimal.ZERO.setScale(2))
                .fromWallet(wallet)
                .merchant(merchant)
                .description(StringUtils.hasText(request.description())
                        ? request.description() : "Thanh toán cho " + merchant.getName())
                .completedAt(Instant.now())
                .build();

        return TransactionResponse.from(transactionRepository.save(tx));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> history(String username,
                                                     TransactionType type,
                                                     TransactionStatus status,
                                                     Instant fromDate,
                                                     Instant toDate,
                                                     Pageable pageable) {
        Wallet wallet = walletRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví của người dùng"));

        // Ghép điều kiện động: điều kiện nào null sẽ tự bị bỏ qua.
        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.involvesWallet(wallet.getId()))
                .and(TransactionSpecifications.hasType(type))
                .and(TransactionSpecifications.hasStatus(status))
                .and(TransactionSpecifications.createdFrom(fromDate))
                .and(TransactionSpecifications.createdTo(toDate));

        var page = transactionRepository.findAll(spec, pageable);
        return PageResponse.of(page, TransactionResponse::from);
    }

    private Optional<Transaction> findByIdempotencyKey(String key) {
        if (!StringUtils.hasText(key)) {
            return Optional.empty();
        }
        return transactionRepository.findByIdempotencyKey(key);
    }
}
