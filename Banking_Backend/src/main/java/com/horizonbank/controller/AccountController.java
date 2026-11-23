package com.horizonbank.controller;

import com.horizonbank.dto.AccountRequest;
import com.horizonbank.dto.AccountResponse;
import com.horizonbank.entity.AccountStatus;
import com.horizonbank.service.AccountService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:5173")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Create a new account
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(
            @Valid @RequestBody AccountRequest request) {
        return accountService.createAccount(request);
    }

    // Get all accounts
    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    // Get account by ID
    @GetMapping("/{id}")
    public AccountResponse getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    // Get account by account number
    @GetMapping("/number/{accountNumber}")
    public AccountResponse getAccountByNumber(
            @PathVariable String accountNumber) {
        return accountService.getAccountByNumber(accountNumber);
    }

    // Get accounts belonging to a customer
    @GetMapping("/customer/{customerId}")
    public List<AccountResponse> getAccountsByCustomer(
            @PathVariable Long customerId) {
        return accountService.getAccountsByCustomer(customerId);
    }

    // Filter accounts by status
    @GetMapping("/status/{status}")
    public List<AccountResponse> getAccountsByStatus(
            @PathVariable AccountStatus status) {
        return accountService.getAccountsByStatus(status);
    }
    
    // APPROVE PENDING ACCOUNT
    @PatchMapping("/{id}/activate")
    public AccountResponse activateAccount(@PathVariable Long id) {
        return accountService.activateAccount(id);
    }

    // FREEZE ACTIVE ACCOUNT
    @PatchMapping("/{id}/freeze")
    public AccountResponse freezeAccount(@PathVariable Long id) {
        return accountService.freezeAccount(id);
    }

    // UNFREEZE ACCOUNT
    @PatchMapping("/{id}/unfreeze")
    public AccountResponse unfreezeAccount(@PathVariable Long id) {
        return accountService.unfreezeAccount(id);
    }

    // CLOSE ACCOUNT
    @PatchMapping("/{id}/close")
    public AccountResponse closeAccount(@PathVariable Long id) {
        return accountService.closeAccount(id);
    }

}