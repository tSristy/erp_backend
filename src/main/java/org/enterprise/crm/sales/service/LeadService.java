package org.enterprise.crm.sales.service;

import org.enterprise.crm.sales.entity.Lead;
import org.enterprise.crm.sales.repository.LeadRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class LeadService extends BaseService<Lead, Long> {

    private final LeadRepository repository;

    public LeadService(LeadRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public org.springframework.data.domain.Page<Lead> searchLeads(Long companyId, String searchTerm, org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(pageable);
    }

}