package com.horizonbank.repository;

import com.horizonbank.entity.Transaction;
import com.horizonbank.entity.TransactionStatus;
import com.horizonbank.entity.TransactionType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find transaction using reference number
    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    // Check duplicate reference number
    boolean existsByReferenceNumber(String referenceNumber);

    // Filter transactions by status
    List<Transaction> findByStatus(TransactionStatus status);

    // Filter transactions by type
    List<Transaction> findByTransactionType(TransactionType transactionType);
}