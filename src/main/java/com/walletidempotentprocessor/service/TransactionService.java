package com.walletidempotentprocessor.service;

import com.walletidempotentprocessor.dto.TransactionRequest;
import com.walletidempotentprocessor.exception.DuplicateTransactionException;
import com.walletidempotentprocessor.exception.InsufficientFundsException;
import com.walletidempotentprocessor.exception.WalletNotFoundException;
import com.walletidempotentprocessor.models.Status;
import com.walletidempotentprocessor.models.Transaction;
import com.walletidempotentprocessor.models.Type;
import com.walletidempotentprocessor.models.Wallet;
import com.walletidempotentprocessor.repository.TransactionRepository;
import com.walletidempotentprocessor.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction process(TransactionRequest req) {
        Wallet wallet = walletRepository.findByIdForUpdate(req.getUserId())
                .orElseThrow(() -> new WalletNotFoundException(req.getUserId()));
        if (transactionRepository.existsById(req.getTransactionId())) {
            throw new DuplicateTransactionException(req.getTransactionId());
        }
        if (req.getType() == Type.DEBIT &&
                wallet.getBalance().compareTo(req.getAmount()) < 0) {
            throw new InsufficientFundsException(req.getUserId());
        }
        BigDecimal newBalance = req.getType() == Type.DEBIT
                ? wallet.getBalance().subtract(req.getAmount())
                : wallet.getBalance().add(req.getAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        Transaction txn = new Transaction();
        txn.setTransactionalId(req.getTransactionId());
        txn.setWallet(wallet);
        txn.setAmount(req.getAmount());
        txn.setType(req.getType());
        txn.setStatus(Status.SUCCESS);
        txn.setCreatedAt(LocalDateTime.now());
        return transactionRepository.save(txn);
    }
}