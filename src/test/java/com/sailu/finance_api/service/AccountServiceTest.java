package com.sailu.finance_api.service;

import com.sailu.finance_api.dto.AccountDto;
import com.sailu.finance_api.dto.CreateAccountRequest;
import com.sailu.finance_api.entity.Account;
import com.sailu.finance_api.entity.AccountType;
import com.sailu.finance_api.entity.User;
import com.sailu.finance_api.entity.UserRole;
import com.sailu.finance_api.exception.InsufficientFundsException;
import com.sailu.finance_api.exception.ResourceNotFoundException;
import com.sailu.finance_api.exception.UnauthorizedException;
import com.sailu.finance_api.repository.AccountRepository;
import com.sailu.finance_api.repository.UserRepository;
import com.sailu.finance_api.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private EntityMapper mapper;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;
    private AccountDto testAccountDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .fullName("Test User")
                .role(UserRole.USER)
                .isActive(true)
                .build();

        testAccount = Account.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .accountNumber("ACC0000000001")
                .accountType(AccountType.SAVINGS)
                .balance(BigDecimal.valueOf(10000))
                .currency("INR")
                .isActive(true)
                .build();

        testAccountDto = AccountDto.builder()
                .id(testAccount.getId())
                .accountNumber("ACC0000000001")
                .accountType(AccountType.SAVINGS)
                .balance(BigDecimal.valueOf(10000))
                .currency("INR")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Get user accounts - returns list")
    void getUserAccounts_ReturnsAccountList() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserIdAndIsActiveTrue(testUser.getId()))
                .thenReturn(List.of(testAccount));
        when(mapper.toAccountDto(testAccount)).thenReturn(testAccountDto);

        List<AccountDto> result = accountService.getUserAccounts("test@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACC0000000001", result.get(0).getAccountNumber());
    }

    @Test
    @DisplayName("Get user accounts - user not found throws exception")
    void getUserAccounts_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.getUserAccounts("unknown@example.com"));
    }

    @Test
    @DisplayName("Create account - success")
    void createAccount_Success() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountType(AccountType.SAVINGS);
        request.setCurrency("INR");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class)))
                .thenReturn(testAccount);
        when(mapper.toAccountDto(testAccount)).thenReturn(testAccountDto);

        AccountDto result = accountService.createAccount(
                "test@example.com", request);

        assertNotNull(result);
        assertEquals(AccountType.SAVINGS, result.getAccountType());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Get balance - success")
    void getBalance_Success() {
        when(accountRepository.findById(testAccount.getId()))
                .thenReturn(Optional.of(testAccount));

        BigDecimal balance = accountService.getBalance(
                "test@example.com", testAccount.getId());

        assertEquals(BigDecimal.valueOf(10000), balance);
    }

    @Test
    @DisplayName("Get balance - unauthorized user throws exception")
    void getBalance_UnauthorizedUser_ThrowsException() {
        when(accountRepository.findById(testAccount.getId()))
                .thenReturn(Optional.of(testAccount));

        assertThrows(UnauthorizedException.class,
                () -> accountService.getBalance(
                        "other@example.com", testAccount.getId()));
    }
}