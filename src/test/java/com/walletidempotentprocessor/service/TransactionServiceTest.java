package com.walletidempotentprocessor.service;

import com.walletidempotentprocessor.dto.TransactionRequest;
import com.walletidempotentprocessor.exception.DuplicateTransactionException;
import com.walletidempotentprocessor.exception.InsufficientFundsException;
import com.walletidempotentprocessor.models.Type;
import com.walletidempotentprocessor.models.Wallet;
import com.walletidempotentprocessor.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletRepository walletRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal("500.00"));
        wallet.setVersion(0);
        walletRepository.save(wallet);
    }

    @Test
    @DisplayName("Processes a single valid debit transaction successfully")
    void happyPath_singleDebit_success() {
        TransactionRequest req = new TransactionRequest();
        req.setTransactionId(UUID.randomUUID());
        req.setUserId(userId);
        req.setAmount(new BigDecimal("100.00"));
        req.setType(Type.DEBIT);

        var result = transactionService.process(req);

        assertEquals("SUCCESS", result.getStatus().name());
        Wallet updated = walletRepository.findByUserId(userId).orElseThrow();
        assertEquals(0, new BigDecimal("400.00").compareTo(updated.getBalance()));
    }

    @Test
    @DisplayName("Sends 3 identical transactionIDs simultaneously — ensures balance is deducted only once")
    void idempotency_threeIdenticalIds_onlyOneSucceeds() throws InterruptedException {
        UUID sharedTransactionId = UUID.randomUUID();
        int threadCount = 3;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    TransactionRequest req = new TransactionRequest();
                    req.setTransactionId(sharedTransactionId);
                    req.setUserId(userId);
                    req.setAmount(new BigDecimal("100.00"));
                    req.setType(Type.DEBIT);
                    transactionService.process(req);
                    successCount.incrementAndGet();
                } catch (DuplicateTransactionException e) {
                    duplicateCount.incrementAndGet();
                } catch (Exception e) {
                    fail("Unexpected exception: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly one request should succeed");
        assertEquals(2, duplicateCount.get(), "Other two should be rejected as duplicates");

        Wallet updated = walletRepository.findByUserId(userId).orElseThrow();
        assertEquals(0, new BigDecimal("400.00").compareTo(updated.getBalance()),
                "Balance should be deducted only ONCE, not three times");
    }

    @Test
    @DisplayName("Sends 10 concurrent ₹100 debits on a ₹500 wallet — final balance is exactly ₹0, 5 fail with insufficient funds")
    void raceCondition_tenConcurrentDebits_exactlyFiveSucceed() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger insufficientFundsCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    TransactionRequest req = new TransactionRequest();
                    req.setTransactionId(UUID.randomUUID());
                    req.setUserId(userId);
                    req.setAmount(new BigDecimal("100.00"));
                    req.setType(Type.DEBIT);
                    transactionService.process(req);
                    successCount.incrementAndGet();
                } catch (InsufficientFundsException e) {
                    insufficientFundsCount.incrementAndGet();
                } catch (Exception e) {
                    fail("Unexpected exception: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(5, successCount.get(), "Exactly 5 debits should succeed");
        assertEquals(5, insufficientFundsCount.get(), "Exactly 5 should fail with insufficient funds");

        Wallet updated = walletRepository.findByUserId(userId).orElseThrow();
        assertEquals(0, BigDecimal.ZERO.compareTo(updated.getBalance()),
                "Final balance must be exactly zero, never negative");
    }
}