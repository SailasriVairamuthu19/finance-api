package com.sailu.finance_api.dto;

import com.sailu.finance_api.entity.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    private String currency = "INR";
}