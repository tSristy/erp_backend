package org.enterprise.organization.repository;

import org.enterprise.organization.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCode(String code);
    boolean existsByCode(String code);
}
