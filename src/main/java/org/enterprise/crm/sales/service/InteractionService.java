package org.enterprise.crm.sales.service;

import org.enterprise.crm.sales.entity.Interaction;
import org.enterprise.crm.sales.repository.InteractionRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InteractionService extends BaseService<Interaction, Long> {

    private final InteractionRepository repository;

    public InteractionService(InteractionRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Interaction> findByOpportunityId(Long opportunityId) {
        return repository.findByOpportunityId(opportunityId);
    }
}
