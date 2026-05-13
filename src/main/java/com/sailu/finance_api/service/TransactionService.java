package com.sailu.finance_api.service;

import com.sailu.finance_api.dto.CreateTransactionRequest;
import com.sailu.finance_api.dto.TransactionDto;
import com.sailu.finance_api.entity.Account;
import com.sailu.finance_api.entity.Transaction;
import com.sailu.finance_api.entity.TransactionType;
import com.sailu.finance_api.exception.InsufficientFundsException;
import com.sailu.finance_api.exception.ResourceNotFoundException;
import com.sailu.finance_api.exception.UnauthorizedException;
import com.sailu.finance_api.repository.AccountRepository;
import com.sailu.finance_api.repository.TransactionRepository;
import com.sailu.finance_api.repository.UserRepository;
import com.sailu.finance_api.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sailu.finance_api.kafka.TransactionEventProducer;
import com.sailu.finance_api.event.TransactionEvent;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final EntityMapper mapper;
    private final AccountService accountService;
    private final TransactionEventProducer eventProducer;

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactions(String email,
                                                UUID accountId,
                                                int page,
                                                int size) {
        Account account = getAccountAndVerify(email, accountId);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return transactionRepository
                .findByAccountId(account.getId(), pageable)
                .map(mapper::toTransactionDto);
    }

    @Transactional
    public TransactionDto createTransaction(String email,
                                            CreateTransactionRequest request) {
        Account account = getAccountAndVerify(email, request.getAccountId());

        if (request.getType() == TransactionType.DEBIT) {
            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException(
                        "Insufficient funds. Available balance: "
                                + account.getBalance());
            }
        }

        if (request.getType() == TransactionType.DEBIT) {
            accountRepository.updateBalance(account.getId(),
                    request.getAmount().negate());
        } else {
            accountRepository.updateBalance(account.getId(),
                    request.getAmount());
        }

        Transaction transaction = Transaction.builder()
                .account(account)
                .type(request.getType())
                .amount(request.getAmount())
                .description(request.getDescription())
                .referenceNumber(generateReferenceNumber())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        TransactionDto dto = mapper.toTransactionDto(saved);

        // evict stale balance cache
        accountService.evictBalanceCache(account.getId());

        // publish event to Kafka
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(saved.getId().toString())
                .accountId(account.getId().toString())
                .userId(account.getUser().getId().toString())
                .type(saved.getType())
                .amount(saved.getAmount())
                .description(saved.getDescription())
                .referenceNumber(saved.getReferenceNumber())
                .status(saved.getStatus())
                .timestamp(saved.getCreatedAt())
                .build();

        eventProducer.publishTransactionEvent(event);

        return dto;
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(String email, UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found: " + transactionId));
        verifyTransactionOwnership(transaction, email);
        return mapper.toTransactionDto(transaction);
    }

    private Account getAccountAndVerify(String email, UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + accountId));
        if (!account.getUser().getEmail().equals(email)) {
            throw new UnauthorizedException(
                    "You do not have access to this account");
        }
        return account;
    }

    private void verifyTransactionOwnership(Transaction transaction,
                                            String email) {
        if (!transaction.getAccount().getUser().getEmail().equals(email)) {
            throw new UnauthorizedException(
                    "You do not have access to this transaction");
        }
    }

    private String generateReferenceNumber() {
        return "REF" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 13)
                .toUpperCase();
    }
}