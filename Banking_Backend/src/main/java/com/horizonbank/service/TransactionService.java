package com.horizonbank.service;
import java.util.List;
import java.util.stream.Collectors;

import com.horizonbank.dto.TransactionResponse;
import com.horizonbank.dto.TransactionEntryResponse;
import com.horizonbank.dto.DepositRequest;
import com.horizonbank.dto.WithdrawalRequest;
import com.horizonbank.dto.TransferRequest;
import com.horizonbank.entity.*;
import com.horizonbank.repository.AccountRepository;
import com.horizonbank.repository.TransactionRepository;
import com.horizonbank.repository.TransactionEntryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionEntryRepository entryRepository;

    public TransactionService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            TransactionEntryRepository entryRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.entryRepository = entryRepository;
    }

    // DEPOSIT
    public Transaction deposit(DepositRequest request) {

        validateAmount(request.getAmount());

        Account account = getLockedAccount(request.getAccountId());
        validateActiveAccount(account);

        account.setAvailableBalance(
                account.getAvailableBalance().add(request.getAmount())
        );

        accountRepository.save(account);

        Transaction transaction = createTransaction(
                TransactionType.DEPOSIT,
                request.getAmount(),
                request.getDescription()
        );

        createEntry(transaction, account, EntryType.CREDIT, request.getAmount());

        return transaction;
    }

    // WITHDRAWAL
    public Transaction withdraw(WithdrawalRequest request) {

        validateAmount(request.getAmount());

        Account account = getLockedAccount(request.getAccountId());
        validateActiveAccount(account);

        if (account.getAvailableBalance().compareTo(request.getAmount()) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient account balance"
            );
        }

        account.setAvailableBalance(
                account.getAvailableBalance().subtract(request.getAmount())
        );

        accountRepository.save(account);

        Transaction transaction = createTransaction(
                TransactionType.WITHDRAWAL,
                request.getAmount(),
                request.getDescription()
        );

        createEntry(transaction, account, EntryType.DEBIT, request.getAmount());

        return transaction;
    }

    // TRANSFER
    public Transaction transfer(TransferRequest request) {

        validateAmount(request.getAmount());

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sender and receiver accounts must be different"
            );
        }

        // Lock accounts in ID order to reduce deadlock risk.
        Account sender;
        Account receiver;

        if (request.getFromAccountId() < request.getToAccountId()) {
            sender = getLockedAccount(request.getFromAccountId());
            receiver = getLockedAccount(request.getToAccountId());
        } else {
            receiver = getLockedAccount(request.getToAccountId());
            sender = getLockedAccount(request.getFromAccountId());
        }

        validateActiveAccount(sender);
        validateActiveAccount(receiver);

        if (sender.getAvailableBalance().compareTo(request.getAmount()) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient sender account balance"
            );
        }

        sender.setAvailableBalance(
                sender.getAvailableBalance().subtract(request.getAmount())
        );

        receiver.setAvailableBalance(
                receiver.getAvailableBalance().add(request.getAmount())
        );

        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction transaction = createTransaction(
                TransactionType.TRANSFER,
                request.getAmount(),
                request.getDescription()
        );

        createEntry(transaction, sender, EntryType.DEBIT, request.getAmount());
        createEntry(transaction, receiver, EntryType.CREDIT, request.getAmount());

        return transaction;
    }

    // FIND AND LOCK ACCOUNT
    private Account getLockedAccount(Long accountId) {

        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found: " + accountId
                ));
    }

    // VALIDATE ACCOUNT STATUS
    private void validateActiveAccount(Account account) {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only ACTIVE accounts can perform transactions"
            );
        }
    }

    // VALIDATE AMOUNT
    private void validateAmount(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transaction amount must be greater than zero"
            );
        }

        if (amount.scale() > 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transaction amount cannot have more than 2 decimal places"
            );
        }
    }

    // CREATE TRANSACTION RECORD
    private Transaction createTransaction(
            TransactionType type,
            BigDecimal amount,
            String description) {

        Transaction transaction = new Transaction();

        transaction.setReferenceNumber(generateReferenceNumber());
        transaction.setTransactionType(type);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setAmount(amount);
        transaction.setDescription(description);

        return transactionRepository.save(transaction);
    }

    // CREATE DEBIT / CREDIT ENTRY
    private void createEntry(
            Transaction transaction,
            Account account,
            EntryType entryType,
            BigDecimal amount) {

        TransactionEntry entry = new TransactionEntry();

        entry.setTransaction(transaction);
        entry.setAccount(account);
        entry.setEntryType(entryType);
        entry.setAmount(amount);

        entryRepository.save(entry);
    }

    // GENERATE UNIQUE REFERENCE NUMBER
    private String generateReferenceNumber() {

        String reference;

        do {
            reference = "HZBTXN"
                    + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 20)
                    .toUpperCase();

        } while (transactionRepository.existsByReferenceNumber(reference));

        return reference;
    }

// GET ALL TRANSACTIONS
@Transactional(readOnly = true)
public List<TransactionResponse> getAllTransactions() {

    return transactionRepository.findAll()
            .stream()
            .map(this::mapToTransactionResponse)
            .collect(Collectors.toList());
}

// GET TRANSACTION BY ID
@Transactional(readOnly = true)
public TransactionResponse getTransactionById(Long id) {

    Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Transaction not found"
            ));

    return mapToTransactionResponse(transaction);
}

// GET TRANSACTION BY REFERENCE NUMBER
@Transactional(readOnly = true)
public TransactionResponse getTransactionByReference(String reference) {

    Transaction transaction = transactionRepository
            .findByReferenceNumber(reference)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Transaction not found"
            ));

    return mapToTransactionResponse(transaction);
}

// GET ACCOUNT TRANSACTION HISTORY
@Transactional(readOnly = true)
public List<TransactionEntryResponse> getAccountTransactionHistory(Long accountId) {

    if (!accountRepository.existsById(accountId)) {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Account not found"
        );
    }

    return entryRepository.findByAccountId(accountId)
            .stream()
            .map(entry -> new TransactionEntryResponse(
                    entry.getId(),
                    entry.getTransaction().getId(),
                    entry.getAccount().getId(),
                    entry.getAccount().getAccountNumber(),
                    entry.getEntryType(),
                    entry.getAmount(),
                    entry.getCreatedAt()
            ))
            .collect(Collectors.toList());
}

// MAP TRANSACTION TO RESPONSE DTO
private TransactionResponse mapToTransactionResponse(Transaction transaction) {

    return new TransactionResponse(
            transaction.getId(),
            transaction.getReferenceNumber(),
            transaction.getTransactionType(),
            transaction.getStatus(),
            transaction.getAmount(),
            transaction.getDescription(),
            transaction.getCreatedAt()
    );
} 
}