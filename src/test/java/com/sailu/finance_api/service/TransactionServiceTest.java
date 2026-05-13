package com.sailu.finance_api.service;

import com.sailu.finance_api.dto.CreateTransactionRequest;
import com.sailu.finance_api.dto.TransactionDto;
import com.sailu.finance_api.entity.*;
import com.sailu.finance_api.exception.InsufficientFundsException;
import com.sailu.finance_api.exception.ResourceNotFoundException;
import com.sailu.finance_api.kafka.TransactionEventProducer;
import com.sailu.finance_api.repository.AccountRepository;
import com.sailu.finance_api.repository.TransactionRepository;
import com.sailu.finance_api.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private EntityMapper mapper;
    @Mock private AccountService accountService;
    @Mock private TransactionEventProducer eventProducer;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Account testAccount;
    private Transaction testTransaction;
    private TransactionDto testTransactionDto;

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

        testTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .account(testAccount)
                .type(TransactionType.CREDIT)
                .amount(BigDecimal.valueOf(5000))
                .description("Test credit")
                .referenceNumber("REF123456789")
                .status(TransactionStatus.SUCCESS)
                .build();

        testTransactionDto = TransactionDto.builder()
                .id(testTransaction.getId())
                .accountId(testAccount.getId())
                .type(TransactionType.CREDIT)
                .amount(BigDecimal.valueOf(5000))
                .referenceNumber("REF123456789")
                .status(TransactionStatus.SUCCESS)
                .build();
    }

    @Test
    @DisplayName("Create transaction - credit success")
    void createTransaction_Credit_Success() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId(testAccount.getId());
        request.setType(TransactionType.CREDIT);
        request.setAmount(BigDecimal.valueOf(5000));
        request.setDescription("Test credit");

        when(accountRepository.findById(testAccount.getId()))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.updateBalance(any(), any())).thenReturn(1);
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(testTransaction);
        when(mapper.toTransactionDto(any(Transaction.class)))
                .thenReturn(testTransactionDto);
        doNothing().when(eventProducer)
                .publishTransactionEvent(any());
        doNothing().when(accountService).evictBalanceCache(any());

        TransactionDto result = transactionService.createTransaction(
                "test@example.com", request);

        assertNotNull(result);
        assertEquals(TransactionType.CREDIT, result.getType());
        assertEquals(BigDecimal.valueOf(5000), result.getAmount());
        verify(accountRepository).updateBalance(any(), any());
        verify(eventProducer).publishTransactionEvent(any());
    }

    @Test
    @DisplayName("Create transaction - debit insufficient funds throws exception")
    void createTransaction_Debit_InsufficientFunds_ThrowsException() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId(testAccount.getId());
        request.setType(TransactionType.DEBIT);
        request.setAmount(BigDecimal.valueOf(99999));
        request.setDescription("Large debit");

        when(accountRepository.findById(testAccount.getId()))
                .thenReturn(Optional.of(testAccount));

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.createTransaction(
                        "test@example.com", request));

        verify(accountRepository, never()).updateBalance(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create transaction - account not found throws exception")
    void createTransaction_AccountNotFound_ThrowsException() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId(UUID.randomUUID());
        request.setType(TransactionType.CREDIT);
        request.setAmount(BigDecimal.valueOf(1000));

        when(accountRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.createTransaction(
                        "test@example.com", request));
    }

    @Test
    @DisplayName("Get transaction by id - success")
    void getTransactionById_Success() {
        when(transactionRepository.findById(testTransaction.getId()))
                .thenReturn(Optional.of(testTransaction));
        when(mapper.toTransactionDto(testTransaction))
                .thenReturn(testTransactionDto);

        TransactionDto result = transactionService.getTransactionById(
                "test@example.com", testTransaction.getId());

        assertNotNull(result);
        assertEquals(testTransactionDto.getId(), result.getId());
    }
}