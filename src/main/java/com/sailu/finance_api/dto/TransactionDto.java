package com.sailu.finance_api.dto;

import com.sailu.finance_api.entity.TransactionStatus;
import com.sailu.finance_api.entity.TransactionType;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionDto implements Serializable {
    private UUID id;
    private UUID accountId;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private String referenceNumber;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}