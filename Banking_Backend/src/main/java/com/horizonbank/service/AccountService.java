package com.horizonbank.service;

import com.horizonbank.dto.AccountRequest;
import com.horizonbank.dto.AccountResponse;
import com.horizonbank.entity.Account;
import com.horizonbank.entity.AccountStatus;
import com.horizonbank.entity.Customer;
import com.horizonbank.repository.AccountRepository;
import com.horizonbank.repository.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository) {

        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    // CREATE ACCOUNT
    public AccountResponse createAccount(AccountRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"));

        Account account = new Account();

        account.setAccountNumber(generateAccountNumber());
        account.setCustomer(customer);
        account.setBranchId(request.getBranchId());
        account.setAccountType(request.getAccountType());
        account.setAccountStatus(AccountStatus.PENDING);
        account.setAvailableBalance(BigDecimal.ZERO);
        account.setCurrency("INR");

        Account savedAccount = accountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    // GET ALL ACCOUNTS
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {

        return accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET ACCOUNT BY ID
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {

        Account account = getAccountEntity(id);

        return mapToResponse(account);
    }

    // GET ACCOUNT BY ACCOUNT NUMBER
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {

        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found"));

        return mapToResponse(account);
    }

    // GET ACCOUNTS BY CUSTOMER ID
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomer(Long customerId) {

        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET ACCOUNTS BY STATUS
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByStatus(AccountStatus status) {

        return accountRepository.findByAccountStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // APPROVE PENDING ACCOUNT
    public AccountResponse activateAccount(Long id) {

        Account account = getAccountEntity(id);

        if (account.getAccountStatus() != AccountStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending accounts can be activated");
        }

        account.setAccountStatus(AccountStatus.ACTIVE);

        return mapToResponse(accountRepository.save(account));
    }

    // FREEZE ACTIVE ACCOUNT
    public AccountResponse freezeAccount(Long id) {

        Account account = getAccountEntity(id);

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only active accounts can be frozen");
        }

        account.setAccountStatus(AccountStatus.FROZEN);

        return mapToResponse(accountRepository.save(account));
    }

    // UNFREEZE ACCOUNT
    public AccountResponse unfreezeAccount(Long id) {

        Account account = getAccountEntity(id);

        if (account.getAccountStatus() != AccountStatus.FROZEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only frozen accounts can be activated again");
        }

        account.setAccountStatus(AccountStatus.ACTIVE);

        return mapToResponse(accountRepository.save(account));
    }

    // CLOSE ACCOUNT
    public AccountResponse closeAccount(Long id) {

        Account account = getAccountEntity(id);

        if (account.getAccountStatus() != AccountStatus.ACTIVE
                && account.getAccountStatus() != AccountStatus.FROZEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only active or frozen accounts can be closed");
        }

        if (account.getAvailableBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Account balance must be zero before closing");
        }

        account.setAccountStatus(AccountStatus.CLOSED);

        return mapToResponse(accountRepository.save(account));
    }

    // FIND ACCOUNT ENTITY
    private Account getAccountEntity(Long id) {

        return accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found"));
    }

    // GENERATE UNIQUE ACCOUNT NUMBER
    private String generateAccountNumber() {

        String accountNumber;

        do {
            accountNumber = "HZB" +
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 13)
                            .toUpperCase();

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    // CONVERT ENTITY TO RESPONSE
    private AccountResponse mapToResponse(Account account) {

        Customer customer = account.getCustomer();

        String customerName = customer.getFirstName()
                + " " + customer.getLastName();

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                customer.getId(),
                customerName,
                account.getBranchId(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getAvailableBalance(),
                account.getCurrency(),
                account.getOpenedAt()
        );
    }
}