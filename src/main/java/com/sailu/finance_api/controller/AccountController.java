package com.sailu.finance_api.controller;

import com.sailu.finance_api.dto.AccountDto;
import com.sailu.finance_api.dto.CreateAccountRequest;
import com.sailu.finance_api.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountDto>> getMyAccounts(Authentication auth) {
        return ResponseEntity.ok(
                accountService.getUserAccounts(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(auth.getName(), request));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccount(
            @PathVariable UUID accountId,
            Authentication auth) {
        return ResponseEntity.ok(
                accountService.getAccountById(auth.getName(), accountId));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable UUID accountId,
            Authentication auth) {
        return ResponseEntity.ok(
                accountService.getBalance(auth.getName(), accountId));
    }
}