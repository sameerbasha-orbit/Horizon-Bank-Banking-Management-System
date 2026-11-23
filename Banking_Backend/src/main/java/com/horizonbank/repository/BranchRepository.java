package com.horizonbank.repository;

import com.horizonbank.entity.Branch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    // Check duplicate branch code
    boolean existsByBranchCode(String branchCode);

    // Check duplicate IFSC code
    boolean existsByIfscCode(String ifscCode);

    // Find branch by branch code
    Optional<Branch> findByBranchCodeIgnoreCase(String branchCode);

    // Check branch code without case sensitivity
    boolean existsByBranchCodeIgnoreCase(String branchCode);

    // Check IFSC code without case sensitivity
    boolean existsByIfscCodeIgnoreCase(String ifscCode);

    // Check duplicate branch code excluding current branch
    boolean existsByBranchCodeIgnoreCaseAndIdNot(
            String branchCode, Long id);

    // Check duplicate IFSC code excluding current branch
    boolean existsByIfscCodeIgnoreCaseAndIdNot(
            String ifscCode, Long id);
}