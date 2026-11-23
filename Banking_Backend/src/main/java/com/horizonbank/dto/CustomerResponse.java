package com.horizonbank.dto;

import com.horizonbank.entity.KycStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerResponse {

    private Long id;
    private String customerCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private KycStatus kycStatus;
    private LocalDateTime createdAt;

    public CustomerResponse() {
    }

    public CustomerResponse(Long id, String customerCode,
            String firstName, String lastName,
            String email, String phone,
            LocalDate dateOfBirth, String address,
            KycStatus kycStatus, LocalDateTime createdAt) {

        this.id = id;
        this.customerCode = customerCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.kycStatus = kycStatus;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getCustomerCode() { return customerCode; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public KycStatus getKycStatus() { return kycStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}