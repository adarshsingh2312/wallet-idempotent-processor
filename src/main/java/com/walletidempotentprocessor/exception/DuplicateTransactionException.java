package com.walletidempotentprocessor.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class DuplicateTransactionException extends RuntimeException {
    private final UUID transactionId;
    public DuplicateTransactionException(UUID transactionId) {
        this.transactionId = transactionId;
    }
}
