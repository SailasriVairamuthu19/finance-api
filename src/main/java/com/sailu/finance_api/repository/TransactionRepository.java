package com.sailu.finance_api.repository;

import com.sailu.finance_api.entity.Transaction;
import com.sailu.finance_api.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // all transactions for an account — paginated
    Page<Transaction> findByAccountId(UUID accountId, Pageable pageable);

    // filter by type
    Page<Transaction> findByAccountIdAndType(
            UUID accountId, TransactionType type, Pageable pageable);

    // filter by date range
    Page<Transaction> findByAccountIdAndCreatedAtBetween(
            UUID accountId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    // sum of credits for an account
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.account.id = :accountId AND t.type = 'CREDIT' " +
            "AND t.status = 'SUCCESS'")
    BigDecimal sumCreditsByAccountId(@Param("accountId") UUID accountId);

    // sum of debits for an account
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.account.id = :accountId AND t.type = 'DEBIT' " +
            "AND t.status = 'SUCCESS'")
    BigDecimal sumDebitsByAccountId(@Param("accountId") UUID accountId);

    // recent transactions — for dashboard summary
    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findRecentByAccountId(
            @Param("accountId") UUID accountId, Pageable pageable);
}