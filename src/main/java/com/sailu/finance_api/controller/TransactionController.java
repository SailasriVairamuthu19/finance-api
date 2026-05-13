package com.sailu.finance_api.controller;

import com.sailu.finance_api.dto.CreateTransactionRequest;
import com.sailu.finance_api.dto.TransactionDto;
import com.sailu.finance_api.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<TransactionDto>> getTransactions(
            @RequestParam UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return ResponseEntity.ok(
                transactionService.getTransactions(
                        auth.getName(), accountId, page, size));
    }

    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(
                        auth.getName(), request));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> getTransaction(
            @PathVariable UUID transactionId,
            Authentication auth) {
        return ResponseEntity.ok(
                transactionService.getTransactionById(
                        auth.getName(), transactionId));
    }
}