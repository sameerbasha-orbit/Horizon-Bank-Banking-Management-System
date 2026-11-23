package com.horizonbank.controller;

import com.horizonbank.dto.CustomerRequest;
import com.horizonbank.dto.CustomerResponse;
import com.horizonbank.entity.KycStatus;
import com.horizonbank.service.CustomerService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:5173")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Register customer
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request) {

        CustomerResponse response =
                customerService.createCustomer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all customers
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    // Get customer by ID
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                customerService.getCustomerById(id));
    }

    // Get customer by customer code
    @GetMapping("/code/{customerCode}")
    public ResponseEntity<CustomerResponse> getCustomerByCode(
            @PathVariable String customerCode) {

        return ResponseEntity.ok(
                customerService.getCustomerByCode(customerCode));
    }

    // Filter customers by KYC status
    @GetMapping("/kyc/{status}")
    public ResponseEntity<List<CustomerResponse>> getCustomersByKycStatus(
            @PathVariable KycStatus status) {

        return ResponseEntity.ok(
                customerService.getCustomersByKycStatus(status));
    }

    // Update customer
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {

        return ResponseEntity.ok(
                customerService.updateCustomer(id, request));
    }
}