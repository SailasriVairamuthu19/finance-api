package com.sailu.finance_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

    @Entity
    @Table(name = "accounts", indexes = {
            @Index(name = "idx_account_user_id", columnList = "user_id"),
            @Index(name = "idx_account_number", columnList = "account_number")
    })
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString(exclude = {"user", "transactions"})
    @EqualsAndHashCode(exclude = {"user", "transactions"})
    public class Account {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Column(name = "account_number", unique = true, nullable = false, length = 20)
        private String accountNumber;

        @Enumerated(EnumType.STRING)
        @Column(name = "account_type", nullable = false, length = 20)
        private AccountType accountType;

        @Column(nullable = false, precision = 15, scale = 2)
        @Builder.Default
        private BigDecimal balance = BigDecimal.ZERO;

        @Column(length = 3)
        @Builder.Default
        private String currency = "INR";

        @Column(name = "is_active")
        @Builder.Default
        private Boolean isActive = true;

        @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @Builder.Default
        private List<Transaction> transactions = new ArrayList<>();

        @CreationTimestamp
        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at")
        private LocalDateTime updatedAt;
    }

