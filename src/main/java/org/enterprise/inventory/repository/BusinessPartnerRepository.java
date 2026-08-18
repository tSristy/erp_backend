package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.BusinessPartner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BusinessPartnerRepository extends JpaRepository<BusinessPartner, Long> {
    Optional<BusinessPartner> findByCodeAndCompanyId(String code, Long companyId);
    
    @Query("SELECT DISTINCT bp FROM BusinessPartner bp JOIN bp.roles r WHERE bp.companyId = :companyId AND r.role = :role")
    List<BusinessPartner> findByCompanyIdAndRole(@Param("companyId") Long companyId, @Param("role") org.enterprise.inventory.entity.BusinessPartnerRole.RoleType role);
}