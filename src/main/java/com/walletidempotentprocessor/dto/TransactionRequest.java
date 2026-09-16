package com.walletidempotentprocessor.dto;

import com.walletidempotentprocessor.models.Type;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
public class TransactionRequest {
    @NonNull
    private UUID transactionId;
    @NonNull
    private UUID userId;
    @NonNull
    @Positive
    private BigDecimal amount;
    @NonNull
    private Type type;
}
