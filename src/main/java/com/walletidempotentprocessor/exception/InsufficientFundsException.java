package com.walletidempotentprocessor.exception;

import lombok.Getter;

import java.util.UUID;
@Getter
public class InsufficientFundsException extends RuntimeException {
    private final UUID walletId;
    public InsufficientFundsException(UUID walletId) {
        this.walletId = walletId;
    }
}
