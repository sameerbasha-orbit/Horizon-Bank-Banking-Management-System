package com.horizonbank.service;

import com.horizonbank.dto.CustomerRequest;
import com.horizonbank.dto.CustomerResponse;
import com.horizonbank.entity.Customer;
import com.horizonbank.entity.KycStatus;
import com.horizonbank.repository.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Register a new customer
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Email already registered");
        }

        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Phone number already registered");
        }

        Customer customer = new Customer();

        customer.setCustomerCode(generateCustomerCode());
        customer.setFirstName(request.getFirstName().trim());
        customer.setLastName(request.getLastName().trim());
        customer.setEmail(request.getEmail().trim().toLowerCase());
        customer.setPhone(request.getPhone().trim());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddress(request.getAddress());

        customer.setKycStatus(KycStatus.PENDING);

        Customer savedCustomer = customerRepository.save(customer);

        return mapToResponse(savedCustomer);
    }

    // Get all customers
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get customer by ID
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Customer not found"));

        return mapToResponse(customer);
    }

    // Get customer by customer code
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByCode(String customerCode) {

        Customer customer = customerRepository
                .findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Customer not found"));

        return mapToResponse(customer);
    }

    // Filter customers by KYC status
    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomersByKycStatus(
            KycStatus kycStatus) {

        return customerRepository.findByKycStatus(kycStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Update customer information
    @Transactional
    public CustomerResponse updateCustomer(
            Long id, CustomerRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Customer not found"));

        String email = request.getEmail().trim().toLowerCase();
        String phone = request.getPhone().trim();

        customerRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Email already registered");
                });

        customerRepository.findByPhone(phone)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Phone number already registered");
                });

        customer.setFirstName(request.getFirstName().trim());
        customer.setLastName(request.getLastName().trim());
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddress(request.getAddress());

        return mapToResponse(customerRepository.save(customer));
    }

    // Generate unique customer code
    private String generateCustomerCode() {

        String customerCode;

        do {
            customerCode = "HZB" +
                    UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();

        } while (customerRepository.existsByCustomerCode(customerCode));

        return customerCode;
    }

    // Convert Entity to Response DTO
    private CustomerResponse mapToResponse(Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerCode(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getDateOfBirth(),
                customer.getAddress(),
                customer.getKycStatus(),
                customer.getCreatedAt()
        );
    }
}