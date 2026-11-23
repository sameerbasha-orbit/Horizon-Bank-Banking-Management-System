package com.horizonbank.dto;

import com.horizonbank.entity.AccountStatus;
import com.horizonbank.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {

    private Long id;
    private String accountNumber;
    private Long customerId;
    private String customerName;
    private Long branchId;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private BigDecimal availableBalance;
    private String currency;
    private LocalDateTime openedAt;

    public AccountResponse() {
    }

    public AccountResponse(
            Long id,
            String accountNumber,
            Long customerId,
            String customerName,
            Long branchId,
            AccountType accountType,
            AccountStatus accountStatus,
            BigDecimal availableBalance,
            String currency,
            LocalDateTime openedAt) {

        this.id = id;
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.branchId = branchId;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.availableBalance = availableBalance;
        this.currency = currency;
        this.openedAt = openedAt;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Long getBranchId() {
        return branchId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }
}