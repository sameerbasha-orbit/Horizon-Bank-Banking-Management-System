package com.horizonbank.controller;

import com.horizonbank.dto.DepositRequest;
import com.horizonbank.dto.WithdrawalRequest;
import com.horizonbank.dto.TransferRequest;
import com.horizonbank.dto.TransactionResponse;
import com.horizonbank.dto.TransactionEntryResponse;
import com.horizonbank.entity.Transaction;
import com.horizonbank.service.TransactionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:5173")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // DEPOSIT
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody DepositRequest request) {

        Transaction transaction = transactionService.deposit(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(transaction));
    }

    // WITHDRAWAL
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody WithdrawalRequest request) {

        Transaction transaction = transactionService.withdraw(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(transaction));
    }

    // TRANSFER
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request) {

        Transaction transaction = transactionService.transfer(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(transaction));
    }

    // GET ALL TRANSACTIONS
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {

        return ResponseEntity.ok(
                transactionService.getAllTransactions()
        );
    }

    // GET TRANSACTION BY ID
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                transactionService.getTransactionById(id)
        );
    }

    // GET BY REFERENCE NUMBER
    @GetMapping("/reference/{reference}")
    public ResponseEntity<TransactionResponse> getByReference(
            @PathVariable String reference) {

        return ResponseEntity.ok(
                transactionService.getTransactionByReference(reference)
        );
    }

    // GET ACCOUNT TRANSACTION HISTORY
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionEntryResponse>> getAccountHistory(
            @PathVariable Long accountId) {

        return ResponseEntity.ok(
                transactionService.getAccountTransactionHistory(accountId)
        );
    }

    private TransactionResponse toResponse(Transaction transaction) {

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