package com.sailu.finance_api.repository;

import com.sailu.finance_api.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserId(UUID userId);

    List<Account> findByUserIdAndIsActiveTrue(UUID userId);

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByIdAndUserId(UUID accountId, UUID userId);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.id = :accountId")
    int updateBalance(@Param("accountId") UUID accountId,
                      @Param("amount") BigDecimal amount);

    @Query("SELECT a.balance FROM Account a WHERE a.id = :accountId")
    Optional<BigDecimal> findBalanceById(@Param("accountId") UUID accountId);
}