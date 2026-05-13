package org.enterprise.crm.sales.service;

import org.enterprise.crm.sales.entity.Opportunity;
import org.enterprise.crm.sales.repository.OpportunityRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpportunityService extends BaseService<Opportunity, Long> {

    private final OpportunityRepository repository;

    public OpportunityService(OpportunityRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Opportunity> findByLeadId(Long leadId) {
        return repository.findByLeadId(leadId);
    }
}
