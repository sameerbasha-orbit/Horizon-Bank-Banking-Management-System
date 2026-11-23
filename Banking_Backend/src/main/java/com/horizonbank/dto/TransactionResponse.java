package com.horizonbank.dto;

import com.horizonbank.entity.TransactionStatus;
import com.horizonbank.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private String referenceNumber;
    private TransactionType transactionType;
    private TransactionStatus status;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;

    public TransactionResponse() {
    }

    public TransactionResponse(
            Long id,
            String referenceNumber,
            TransactionType transactionType,
            TransactionStatus status,
            BigDecimal amount,
            String description,
            LocalDateTime createdAt) {

        this.id = id;
        this.referenceNumber = referenceNumber;
        this.transactionType = transactionType;
        this.status = status;
        this.amount = amount;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
