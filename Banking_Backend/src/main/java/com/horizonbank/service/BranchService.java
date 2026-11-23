package com.horizonbank.service;

import com.horizonbank.dto.BranchRequest;
import com.horizonbank.dto.BranchResponse;
import com.horizonbank.entity.Branch;
import com.horizonbank.repository.BranchRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    // Create a new branch
    @Transactional
    public BranchResponse createBranch(BranchRequest request) {

        String branchCode = request.getBranchCode().trim().toUpperCase();
        String ifscCode = request.getIfscCode().trim().toUpperCase();

        if (branchRepository.existsByBranchCodeIgnoreCase(branchCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Branch code already exists");
        }

        if (branchRepository.existsByIfscCodeIgnoreCase(ifscCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "IFSC code already exists");
        }

        Branch branch = new Branch();

        branch.setBranchCode(branchCode);
        branch.setBranchName(request.getBranchName().trim());
        branch.setIfscCode(ifscCode);
        branch.setAddress(request.getAddress().trim());
        branch.setCity(request.getCity().trim());
        branch.setState(request.getState().trim());

        Branch savedBranch = branchRepository.save(branch);

        return mapToResponse(savedBranch);
    }

    // Get all branches
    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {

        return branchRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get branch by ID
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Branch not found"));

        return mapToResponse(branch);
    }

    // Get branch by branch code
    @Transactional(readOnly = true)
    public BranchResponse getBranchByCode(String branchCode) {

        Branch branch = branchRepository
                .findByBranchCodeIgnoreCase(branchCode.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Branch not found"));

        return mapToResponse(branch);
    }

    // Update branch information
    @Transactional
    public BranchResponse updateBranch(Long id, BranchRequest request) {

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Branch not found"));

        String branchCode = request.getBranchCode().trim().toUpperCase();
        String ifscCode = request.getIfscCode().trim().toUpperCase();

        if (branchRepository.existsByBranchCodeIgnoreCaseAndIdNot(
                branchCode, id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Branch code already exists");
        }

        if (branchRepository.existsByIfscCodeIgnoreCaseAndIdNot(
                ifscCode, id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "IFSC code already exists");
        }

        branch.setBranchCode(branchCode);
        branch.setBranchName(request.getBranchName().trim());
        branch.setIfscCode(ifscCode);
        branch.setAddress(request.getAddress().trim());
        branch.setCity(request.getCity().trim());
        branch.setState(request.getState().trim());

        Branch updatedBranch = branchRepository.save(branch);

        return mapToResponse(updatedBranch);
    }

    // Delete branch
    @Transactional
    public void deleteBranch(Long id) {

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Branch not found"));

        branchRepository.delete(branch);
    }

    // Convert Entity to Response DTO
    private BranchResponse mapToResponse(Branch branch) {

        return new BranchResponse(
                branch.getId(),
                branch.getBranchCode(),
                branch.getBranchName(),
                branch.getIfscCode(),
                branch.getAddress(),
                branch.getCity(),
                branch.getState(),
                branch.getCreatedAt()
        );
    }
}