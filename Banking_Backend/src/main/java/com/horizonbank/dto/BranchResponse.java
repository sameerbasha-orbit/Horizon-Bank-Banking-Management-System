package com.horizonbank.dto;

import java.time.LocalDateTime;

public class BranchResponse {

    private Long id;
    private String branchCode;
    private String branchName;
    private String ifscCode;
    private String address;
    private String city;
    private String state;
    private LocalDateTime createdAt;

    public BranchResponse() {
    }

    public BranchResponse(
            Long id,
            String branchCode,
            String branchName,
            String ifscCode,
            String address,
            String city,
            String state,
            LocalDateTime createdAt) {

        this.id = id;
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.ifscCode = ifscCode;
        this.address = address;
        this.city = city;
        this.state = state;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}