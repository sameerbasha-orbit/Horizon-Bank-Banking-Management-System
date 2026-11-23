package com.horizonbank.dto;

import com.horizonbank.entity.EntryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionEntryResponse {

    private Long id;
    private Long transactionId;
    private Long accountId;
    private String accountNumber;
    private EntryType entryType;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    public TransactionEntryResponse() {
    }

    public TransactionEntryResponse(
            Long id,
            Long transactionId,
            Long accountId,
            String accountNumber,
            EntryType entryType,
            BigDecimal amount,
            LocalDateTime createdAt) {

        this.id = id;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.entryType = entryType;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
