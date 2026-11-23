package com.horizonbank.controller;

import com.horizonbank.dto.BranchRequest;
import com.horizonbank.dto.BranchResponse;
import com.horizonbank.service.BranchService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@CrossOrigin(origins = "http://localhost:5173")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    // Create branch
    @PostMapping
    public ResponseEntity<BranchResponse> createBranch(
            @Valid @RequestBody BranchRequest request) {

        BranchResponse response =
                branchService.createBranch(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all branches
    @GetMapping
    public ResponseEntity<List<BranchResponse>> getAllBranches() {

        return ResponseEntity.ok(
                branchService.getAllBranches());
    }

    // Get branch by ID
    @GetMapping("/{id}")
    public ResponseEntity<BranchResponse> getBranchById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                branchService.getBranchById(id));
    }

    // Get branch by branch code
    @GetMapping("/code/{branchCode}")
    public ResponseEntity<BranchResponse> getBranchByCode(
            @PathVariable String branchCode) {

        return ResponseEntity.ok(
                branchService.getBranchByCode(branchCode));
    }

    // Update branch
    @PutMapping("/{id}")
    public ResponseEntity<BranchResponse> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchRequest request) {

        return ResponseEntity.ok(
                branchService.updateBranch(id, request));
    }

    // Delete branch
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBranch(
            @PathVariable Long id) {

        branchService.deleteBranch(id);

        return ResponseEntity.noContent().build();
    }
}