package com.sailu.finance_api.service;

import com.sailu.finance_api.dto.AccountDto;
import com.sailu.finance_api.dto.CreateAccountRequest;
import com.sailu.finance_api.entity.Account;
import com.sailu.finance_api.entity.User;
import com.sailu.finance_api.exception.ResourceNotFoundException;
import com.sailu.finance_api.exception.UnauthorizedException;
import com.sailu.finance_api.repository.AccountRepository;
import com.sailu.finance_api.repository.UserRepository;
import com.sailu.finance_api.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final EntityMapper mapper;

    @Cacheable(value = "accounts", key = "#email")
    @Transactional(readOnly = true)
    public List<AccountDto> getUserAccounts(String email) {
        log.info("Fetching accounts from DB for user: {}", email);
        User user = getUserByEmail(email);
        return accountRepository.findByUserIdAndIsActiveTrue(user.getId())
                .stream()
                .map(mapper::toAccountDto)
                .collect(Collectors.toList());
    }

    @Caching(evict = {
            @CacheEvict(value = "accounts", key = "#email")
    })
    @Transactional
    public AccountDto createAccount(String email, CreateAccountRequest request) {
        log.info("Creating account for user: {}", email);
        User user = getUserByEmail(email);
        Account account = Account.builder()
                .user(user)
                .accountNumber(generateAccountNumber())
                .accountType(request.getAccountType())
                .currency(request.getCurrency())
                .balance(BigDecimal.ZERO)
                .build();
        return mapper.toAccountDto(accountRepository.save(account));
    }

    @Cacheable(value = "account-balance", key = "#accountId")
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String email, UUID accountId) {
        log.info("Fetching balance from DB for account: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + accountId));
        verifyOwnership(account, email);
        return account.getBalance();
    }

    @CacheEvict(value = "account-balance", key = "#accountId")
    public void evictBalanceCache(UUID accountId) {
        log.info("Evicting balance cache for account: {}", accountId);
    }

    @Cacheable(value = "accounts", key = "#accountId")
    @Transactional(readOnly = true)
    public AccountDto getAccountById(String email, UUID accountId) {
        log.info("Fetching account from DB: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + accountId));
        verifyOwnership(account, email);
        return mapper.toAccountDto(account);
    }

    private void verifyOwnership(Account account, String email) {
        if (!account.getUser().getEmail().equals(email)) {
            throw new UnauthorizedException(
                    "You do not have access to this account");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));
    }

    private String generateAccountNumber() {
        return "ACC" + String.format("%010d",
                (long) (Math.random() * 9_000_000_000L) + 1_000_000_000L);
    }
}