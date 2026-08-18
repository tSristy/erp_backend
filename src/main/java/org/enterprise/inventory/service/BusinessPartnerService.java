package org.enterprise.inventory.service;

import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.repository.BusinessPartnerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BusinessPartnerService extends BaseService<BusinessPartner, Long> {

    private final BusinessPartnerRepository repository;

    public BusinessPartnerService(BusinessPartnerRepository repository) {
        super(repository); // 🔥 required
        this.repository = repository;
    }

    public Optional<BusinessPartner> findByCode(String code, Long companyId) {
        return repository.findByCodeAndCompanyId(code, companyId);
    }

    public java.util.List<BusinessPartner> findAllByRole(org.enterprise.inventory.entity.BusinessPartnerRole.RoleType role) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        return repository.findByCompanyIdAndRole(companyId, role);
    }
}