package com.sailu.finance_api.event;

import com.sailu.finance_api.entity.TransactionStatus;
import com.sailu.finance_api.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    private String transactionId;
    private String accountId;
    private String userId;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private String referenceNumber;
    private TransactionStatus status;
    private LocalDateTime timestamp;
}