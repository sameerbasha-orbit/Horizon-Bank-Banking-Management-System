package com.horizonbank.repository;

import com.horizonbank.entity.TransactionEntry;
import com.horizonbank.entity.EntryType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionEntryRepository
        extends JpaRepository<TransactionEntry, Long> {

    // Get all entries for a transaction
    List<TransactionEntry> findByTransactionId(Long transactionId);

    // Get transaction entries for an account
    List<TransactionEntry> findByAccountId(Long accountId);

    // Filter entries by account and entry type
    List<TransactionEntry> findByAccountIdAndEntryType(
            Long accountId, EntryType entryType);
}