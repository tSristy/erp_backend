package org.enterprise.crm.sales.service;

import org.enterprise.common.util.TenantContext;
import org.enterprise.crm.sales.entity.Interaction;
import org.enterprise.crm.sales.entity.Lead;
import org.enterprise.crm.sales.entity.Opportunity;
import org.enterprise.crm.sales.repository.InteractionRepository;
import org.enterprise.crm.sales.repository.LeadRepository;
import org.enterprise.crm.sales.repository.OpportunityRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InteractionService extends BaseService<Interaction, Long> {

    private final InteractionRepository repository;
    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;

    public InteractionService(
            InteractionRepository repository,
            LeadRepository leadRepository,
            OpportunityRepository opportunityRepository) {
        super(repository);
        this.repository = repository;
        this.leadRepository = leadRepository;
        this.opportunityRepository = opportunityRepository;
    }

    @Override
    @Transactional
    public Interaction save(Interaction interaction) {
        if (interaction.getCompanyId() == null) {
            interaction.setCompanyId(TenantContext.getCompanyId());
        }
        if (interaction.getInteractionDate() == null) {
            interaction.setInteractionDate(LocalDateTime.now());
        }

        Long oppId = interaction.resolveOpportunityId();
        Opportunity linkedOpp = null;
        if (oppId != null) {
            linkedOpp = opportunityRepository.findById(oppId).orElse(null);
            interaction.setOpportunity(linkedOpp);
        } else {
            interaction.setOpportunity(null);
        }

        Long leadId = interaction.resolveLeadId();
        if (leadId == null && linkedOpp != null && linkedOpp.getLead() != null) {
            leadId = linkedOpp.getLead().getId();
        }
        Lead linkedLead = null;
        if (leadId != null) {
            linkedLead = leadRepository.findById(leadId).orElse(null);
            interaction.setLead(linkedLead);
        } else {
            interaction.setLead(null);
        }

        Interaction saved = super.save(interaction);

        // Automatically advance a NEW lead to CONTACTED when first interaction is logged
        if (linkedLead != null && linkedLead.getStatus() == Lead.LeadStatus.NEW) {
            linkedLead.setStatus(Lead.LeadStatus.CONTACTED);
            leadRepository.save(linkedLead);
        }

        return saved;
    }

    public List<Interaction> findByOpportunityId(Long opportunityId) {
        return repository.findByOpportunityId(opportunityId);
    }

    public Page<Interaction> searchInteractions(Long companyId, String searchTerm, Pageable pageable) {
        return repository.searchByQuery(searchTerm, pageable);
    }

}