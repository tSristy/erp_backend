package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.BusinessPartner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessPartnerRepository extends JpaRepository<BusinessPartner, Long> {
    Optional<BusinessPartner> findByCodeAndCompanyId(String code, Long companyId);
}