package com.walletidempotentprocessor.dto;

import com.walletidempotentprocessor.models.Status;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;
@Getter
@Setter
public class TransactionResponse {
    private UUID transactionId;
    private Status status;
    BigDecimal newBalance;
}
