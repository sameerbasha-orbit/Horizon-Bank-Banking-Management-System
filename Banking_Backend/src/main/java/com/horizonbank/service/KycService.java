package com.horizonbank.service;

import com.horizonbank.dto.CustomerResponse;
import com.horizonbank.entity.Customer;
import com.horizonbank.entity.KycStatus;
import com.horizonbank.repository.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class KycService {

    private final CustomerRepository customerRepository;

    public KycService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Get customer KYC details
    @Transactional(readOnly = true)
    public CustomerResponse getKycDetails(Long customerId) {

        Customer customer = getCustomer(customerId);

        return mapToResponse(customer);
    }

    // Get all pending KYC applications
    @Transactional(readOnly = true)
    public List<CustomerResponse> getPendingKyc() {

        return customerRepository.findByKycStatus(KycStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Approve KYC
    @Transactional
    public CustomerResponse approveKyc(Long customerId) {

        Customer customer = getCustomer(customerId);

        if (customer.getKycStatus() == KycStatus.VERIFIED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer KYC is already verified");
        }

        customer.setKycStatus(KycStatus.VERIFIED);

        return mapToResponse(customerRepository.save(customer));
    }

    // Reject KYC
    @Transactional
    public CustomerResponse rejectKyc(Long customerId) {

        Customer customer = getCustomer(customerId);

        if (customer.getKycStatus() == KycStatus.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer KYC is already rejected");
        }

        customer.setKycStatus(KycStatus.REJECTED);

        return mapToResponse(customerRepository.save(customer));
    }

    // Find customer
    private Customer getCustomer(Long customerId) {

        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found"));
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