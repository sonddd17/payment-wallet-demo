package com.sonddd.paywallet.config;

import com.sonddd.paywallet.entity.Merchant;
import com.sonddd.paywallet.entity.Transaction;
import com.sonddd.paywallet.entity.User;
import com.sonddd.paywallet.entity.Wallet;
import com.sonddd.paywallet.enums.Role;
import com.sonddd.paywallet.enums.TransactionStatus;
import com.sonddd.paywallet.enums.TransactionType;
import com.sonddd.paywallet.enums.WalletStatus;
import com.sonddd.paywallet.repository.MerchantRepository;
import com.sonddd.paywallet.repository.TransactionRepository;
import com.sonddd.paywallet.repository.UserRepository;
import com.sonddd.paywallet.repository.WalletRepository;
import com.sonddd.paywallet.util.References;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Nạp dữ liệu mẫu khi khởi động (nếu CSDL trống) để có thể demo ngay:
 * 1 admin, 2 người dùng, 2 merchant và vài giao dịch mẫu.
 * Không chạy ở profile "test" để không ảnh hưởng integration test.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String CURRENCY = "VND";

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      WalletRepository walletRepository,
                      MerchantRepository merchantRepository,
                      TransactionRepository transactionRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Dữ liệu đã tồn tại, bỏ qua bước seed.");
            return;
        }

        log.info("Đang nạp dữ liệu mẫu...");

        createUserWithWallet("admin", "admin@paywallet.local", "admin123", "Quản trị viên", Role.ADMIN, vnd("0"));
        Wallet aliceWallet = createUserWithWallet("alice", "alice@paywallet.local", "alice123", "Nguyễn Thị An", Role.USER, vnd("750000"));
        Wallet bobWallet = createUserWithWallet("bob", "bob@paywallet.local", "bob123", "Trần Văn Bình", Role.USER, vnd("500000"));

        Merchant coffee = merchantRepository.save(Merchant.builder()
                .code("COFFEE").name("Highlands Coffee (demo)").balance(vnd("50000")).active(true).build());
        Merchant shop = merchantRepository.save(Merchant.builder()
                .code("SHOP").name("Demo Online Shop").balance(vnd("0")).active(true).build());

        Instant now = Instant.now();
        // Một vài giao dịch mẫu (đã hoàn tất) để lịch sử và báo cáo có dữ liệu.
        transactionRepository.save(Transaction.builder()
                .reference(References.newTransactionRef()).type(TransactionType.TOPUP)
                .status(TransactionStatus.SUCCESS).amount(vnd("1000000")).fee(vnd("0"))
                .toWallet(aliceWallet).description("Nạp tiền vào ví")
                .completedAt(now.minus(3, ChronoUnit.DAYS)).build());

        transactionRepository.save(Transaction.builder()
                .reference(References.newTransactionRef()).type(TransactionType.TOPUP)
                .status(TransactionStatus.SUCCESS).amount(vnd("300000")).fee(vnd("0"))
                .toWallet(bobWallet).description("Nạp tiền vào ví")
                .completedAt(now.minus(2, ChronoUnit.DAYS)).build());

        transactionRepository.save(Transaction.builder()
                .reference(References.newTransactionRef()).type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS).amount(vnd("200000")).fee(vnd("0"))
                .fromWallet(aliceWallet).toWallet(bobWallet).description("Chuyển tiền tới bob")
                .completedAt(now.minus(1, ChronoUnit.DAYS)).build());

        transactionRepository.save(Transaction.builder()
                .reference(References.newTransactionRef()).type(TransactionType.PAYMENT)
                .status(TransactionStatus.SUCCESS).amount(vnd("50000")).fee(vnd("0"))
                .fromWallet(aliceWallet).merchant(coffee).description("Thanh toán cho Highlands Coffee (demo)")
                .completedAt(now.minus(6, ChronoUnit.HOURS)).build());

        log.info("Seed xong. Tài khoản demo: admin/admin123, alice/alice123, bob/bob123. Merchant: COFFEE, SHOP.");
    }

    private Wallet createUserWithWallet(String username, String email, String rawPassword,
                                        String fullName, Role role, BigDecimal balance) {
        User user = userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .role(role)
                .enabled(true)
                .build());

        return walletRepository.save(Wallet.builder()
                .user(user)
                .balance(balance)
                .currency(CURRENCY)
                .status(WalletStatus.ACTIVE)
                .build());
    }

    private static BigDecimal vnd(String value) {
        return new BigDecimal(value).setScale(2);
    }
}
