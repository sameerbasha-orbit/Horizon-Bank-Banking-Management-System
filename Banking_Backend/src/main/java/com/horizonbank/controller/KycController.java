package com.horizonbank.controller;

import com.horizonbank.dto.CustomerResponse;
import com.horizonbank.service.KycService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kyc")
@CrossOrigin(origins = "http://localhost:5173")
public class KycController {

    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    // Get customer KYC details
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomerResponse> getKycDetails(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                kycService.getKycDetails(customerId));
    }

    // Get all pending KYC applications
    @GetMapping("/pending")
    public ResponseEntity<List<CustomerResponse>> getPendingKyc() {

        return ResponseEntity.ok(
                kycService.getPendingKyc());
    }

    // Approve KYC
    @PatchMapping("/{customerId}/approve")
    public ResponseEntity<CustomerResponse> approveKyc(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                kycService.approveKyc(customerId));
    }

    // Reject KYC
    @PatchMapping("/{customerId}/reject")
    public ResponseEntity<CustomerResponse> rejectKyc(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                kycService.rejectKyc(customerId));
    }
}