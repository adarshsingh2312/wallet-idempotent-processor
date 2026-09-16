package com.walletidempotentprocessor.controller;

import com.walletidempotentprocessor.dto.TransactionRequest;
import com.walletidempotentprocessor.models.Transaction;
import com.walletidempotentprocessor.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/process")
    public ResponseEntity<Transaction> process(@Valid @RequestBody TransactionRequest req) {
        Transaction result = transactionService.process(req);
        return ResponseEntity.ok(result);
    }
}
