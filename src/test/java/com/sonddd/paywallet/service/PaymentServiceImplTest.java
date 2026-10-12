package com.sonddd.paywallet.service;

import com.sonddd.paywallet.dto.request.TransferRequest;
import com.sonddd.paywallet.dto.response.TransactionResponse;
import com.sonddd.paywallet.entity.Transaction;
import com.sonddd.paywallet.entity.User;
import com.sonddd.paywallet.entity.Wallet;
import com.sonddd.paywallet.enums.TransactionStatus;
import com.sonddd.paywallet.enums.TransactionType;
import com.sonddd.paywallet.enums.WalletStatus;
import com.sonddd.paywallet.exception.BusinessException;
import com.sonddd.paywallet.exception.InsufficientBalanceException;
import com.sonddd.paywallet.repository.MerchantRepository;
import com.sonddd.paywallet.repository.TransactionRepository;
import com.sonddd.paywallet.repository.WalletRepository;
import com.sonddd.paywallet.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test cho PaymentServiceImpl dùng Mockito để giả lập tầng repository.
 * Tập trung kiểm thử quy tắc nghiệp vụ chuyển tiền mà KHÔNG cần CSDL thật.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService - chuyển tiền")
class PaymentServiceImplTest {

    @Mock WalletRepository walletRepository;
    @Mock MerchantRepository merchantRepository;
    @Mock TransactionRepository transactionRepository;

    @InjectMocks PaymentServiceImpl paymentService;

    private Wallet aliceWallet;
    private Wallet bobWallet;

    @BeforeEach
    void setUp() {
        aliceWallet = wallet(1L, "alice", "1000000");
        bobWallet = wallet(2L, "bob", "0");
    }

    @Test
    @DisplayName("Chuyển tiền thành công: trừ ví người gửi, cộng ví người nhận")
    void transfer_success() {
        when(walletRepository.findByUser_Username("alice")).thenReturn(Optional.of(aliceWallet));
        when(walletRepository.findByUser_Username("bob")).thenReturn(Optional.of(bobWallet));
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(aliceWallet));
        when(walletRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(bobWallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferRequest req = new TransferRequest("bob", new BigDecimal("200000"), "Tiền ăn trưa", null);
        TransactionResponse res = paymentService.transfer("alice", req);

        assertThat(aliceWallet.getBalance()).isEqualByComparingTo("800000");
        assertThat(bobWallet.getBalance()).isEqualByComparingTo("200000");
        assertThat(res.type()).isEqualTo(TransactionType.TRANSFER.name());
        assertThat(res.status()).isEqualTo(TransactionStatus.SUCCESS.name());
        assertThat(res.amount()).isEqualByComparingTo("200000");
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Số dư không đủ: ném InsufficientBalanceException, không lưu giao dịch")
    void transfer_insufficientBalance() {
        aliceWallet.setBalance(new BigDecimal("100.00"));
        when(walletRepository.findByUser_Username("alice")).thenReturn(Optional.of(aliceWallet));
        when(walletRepository.findByUser_Username("bob")).thenReturn(Optional.of(bobWallet));
        when(walletRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(aliceWallet));
        when(walletRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(bobWallet));

        TransferRequest req = new TransferRequest("bob", new BigDecimal("200000"), null, null);

        assertThatThrownBy(() -> paymentService.transfer("alice", req))
                .isInstanceOf(InsufficientBalanceException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Không cho tự chuyển cho chính mình")
    void transfer_toSelf_rejected() {
        TransferRequest req = new TransferRequest("alice", new BigDecimal("1000"), null, null);

        assertThatThrownBy(() -> paymentService.transfer("alice", req))
                .isInstanceOf(BusinessException.class);
        verify(walletRepository, never()).findByIdForUpdate(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Idempotency: gửi lại cùng key trả về giao dịch cũ, không chuyển tiền lần 2")
    void transfer_idempotent() {
        Transaction existing = Transaction.builder()
                .id(99L).reference("TXN-OLD").type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS).amount(new BigDecimal("200000")).fee(BigDecimal.ZERO)
                .fromWallet(aliceWallet).toWallet(bobWallet).build();
        when(transactionRepository.findByIdempotencyKey("key-123")).thenReturn(Optional.of(existing));

        TransferRequest req = new TransferRequest("bob", new BigDecimal("200000"), null, "key-123");
        TransactionResponse res = paymentService.transfer("alice", req);

        assertThat(res.reference()).isEqualTo("TXN-OLD");
        assertThat(aliceWallet.getBalance()).isEqualByComparingTo("1000000"); // không đổi
        verify(walletRepository, never()).findByIdForUpdate(any());
        verify(transactionRepository, never()).save(any());
    }

    // ----- helpers -----
    private Wallet wallet(Long id, String username, String balance) {
        User user = User.builder().id(id).username(username).build();
        return Wallet.builder()
                .id(id).user(user)
                .balance(new BigDecimal(balance).setScale(2))
                .currency("VND").status(WalletStatus.ACTIVE).version(0L)
                .build();
    }
}
