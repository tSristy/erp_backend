package org.enterprise.organization.repository;

import org.enterprise.organization.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long>, JpaSpecificationExecutor<Branch> {
    Optional<Branch> findTopByCodeStartingWithOrderByIdDesc(String prefix);
}
