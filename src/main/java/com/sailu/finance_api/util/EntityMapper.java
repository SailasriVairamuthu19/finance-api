package com.sailu.finance_api.util;

import com.sailu.finance_api.dto.AccountDto;
import com.sailu.finance_api.dto.TransactionDto;
import com.sailu.finance_api.entity.Account;
import com.sailu.finance_api.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public AccountDto toAccountDto(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .isActive(account.getIsActive())
                .createdAt(account.getCreatedAt())
                .build();
    }

    public TransactionDto toTransactionDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .referenceNumber(transaction.getReferenceNumber())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}