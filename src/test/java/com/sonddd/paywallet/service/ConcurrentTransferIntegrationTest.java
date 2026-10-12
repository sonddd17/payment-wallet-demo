package com.sonddd.paywallet.service;

import com.sonddd.paywallet.dto.request.RegisterRequest;
import com.sonddd.paywallet.dto.request.TopUpRequest;
import com.sonddd.paywallet.dto.request.TransferRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm thử truy cập đồng thời: nhiều luồng cùng chuyển tiền từ MỘT ví.
 * Với khóa bi quan đúng cách, các giao dịch phải xếp hàng lần lượt:
 * tất cả đều thành công và tổng tiền không bị sai (không mất cập nhật).
 */
@SpringBootTest
@AutoConfigureMockMvc   // dùng chung Spring context (và CSDL test) với các integration test khác
@ActiveProfiles("test")
@DisplayName("Chuyển tiền đồng thời")
class ConcurrentTransferIntegrationTest {

    private static final int THREADS = 10;

    @Autowired AuthService authService;
    @Autowired WalletService walletService;
    @Autowired PaymentService paymentService;

    @Test
    @DisplayName("10 luồng cùng chuyển 10.000 từ ví 100.000: tất cả thành công, số dư cuối đúng")
    void concurrentTransfers_areSerialized() throws Exception {
        authService.register(new RegisterRequest("sender1", "sender1@test.local", "secret123", null));
        authService.register(new RegisterRequest("receiver1", "receiver1@test.local", "secret123", null));
        walletService.topUp("sender1", new TopUpRequest(new BigDecimal("100000"), null, null));

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            results.add(pool.submit(() -> {
                start.await();   // cho tất cả luồng xuất phát cùng lúc
                paymentService.transfer("sender1",
                        new TransferRequest("receiver1", new BigDecimal("10000"), null, null));
                return null;
            }));
        }
        start.countDown();

        List<String> failures = new ArrayList<>();
        for (Future<?> f : results) {
            try {
                f.get(30, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                failures.add(e.getCause().getClass().getSimpleName());
            }
        }
        pool.shutdown();

        assertThat(failures).as("các giao dịch hợp lệ không được thất bại").isEmpty();
        assertThat(walletService.getMyWallet("sender1").balance()).isEqualByComparingTo("0");
        assertThat(walletService.getMyWallet("receiver1").balance()).isEqualByComparingTo("100000");
    }
}
