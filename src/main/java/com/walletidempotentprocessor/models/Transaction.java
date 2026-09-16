package com.walletidempotentprocessor.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@Entity
public class Transaction {
    @Id
    UUID transactionalId;
    @ManyToOne
    @JoinColumn(name = "wallet_id")
    Wallet wallet;
    BigDecimal amount;
    @Enumerated(EnumType.STRING)
    Type type;
    @Enumerated(EnumType.STRING)
    Status status;
    @CreationTimestamp
    LocalDateTime createdAt;
}