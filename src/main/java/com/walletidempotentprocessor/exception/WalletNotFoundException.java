package com.walletidempotentprocessor.exception;

import lombok.Getter;

import java.util.UUID;
@Getter
public class WalletNotFoundException extends RuntimeException {
    private final UUID userId;
    public WalletNotFoundException(UUID userId) {
        this.userId = userId;
    }
}
