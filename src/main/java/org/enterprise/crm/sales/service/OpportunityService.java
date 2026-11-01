package org.enterprise.crm.sales.service;

import org.enterprise.common.util.TenantContext;
import org.enterprise.crm.sales.entity.Lead;
import org.enterprise.crm.sales.entity.Opportunity;
import org.enterprise.crm.sales.repository.LeadRepository;
import org.enterprise.crm.sales.repository.OpportunityRepository;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.repository.BusinessPartnerRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OpportunityService extends BaseService<Opportunity, Long> {

    private final OpportunityRepository repository;
    private final LeadRepository leadRepository;
    private final BusinessPartnerRepository businessPartnerRepository;

    public OpportunityService(
            OpportunityRepository repository,
            LeadRepository leadRepository,
            BusinessPartnerRepository businessPartnerRepository) {
        super(repository);
        this.repository = repository;
        this.leadRepository = leadRepository;
        this.businessPartnerRepository = businessPartnerRepository;
    }

    @Override
    @Transactional
    public Opportunity save(Opportunity opportunity) {
        if (opportunity.getCompanyId() == null) {
            opportunity.setCompanyId(TenantContext.getCompanyId());
        }

        Long leadId = opportunity.resolveLeadId();
        Lead linkedLead = null;
        if (leadId != null) {
            linkedLead = leadRepository.findById(leadId).orElse(null);
            opportunity.setLead(linkedLead);
        } else {
            opportunity.setLead(null);
        }

        Long customerId = opportunity.resolveCustomerId();
        if (customerId == null && linkedLead != null && linkedLead.getConvertedCustomerId() != null) {
            customerId = linkedLead.getConvertedCustomerId();
        }
        if (customerId != null) {
            BusinessPartner customer = businessPartnerRepository.findById(customerId).orElse(null);
            opportunity.setCustomer(customer);
        } else {
            opportunity.setCustomer(null);
        }

        Opportunity saved = super.save(opportunity);

        if (linkedLead != null) {
            boolean leadChanged = false;
            if (linkedLead.getStatus() == Lead.LeadStatus.NEW || linkedLead.getStatus() == Lead.LeadStatus.CONTACTED) {
                linkedLead.setStatus(Lead.LeadStatus.QUALIFIED);
                leadChanged = true;
            }
            if (linkedLead.getConvertedOpportunityId() == null) {
                linkedLead.setConvertedOpportunityId(saved.getId());
                leadChanged = true;
            }
            if (saved.getCustomer() != null && linkedLead.getConvertedCustomerId() == null) {
                linkedLead.setConvertedCustomerId(saved.getCustomer().getId());
                leadChanged = true;
            }
            if (leadChanged) {
                leadRepository.save(linkedLead);
            }
        }

        return saved;
    }

    public List<Opportunity> findByLeadId(Long leadId) {
        return repository.findByLeadId(leadId);
    }

    public Page<Opportunity> searchOpportunitys(Long companyId, String searchTerm, Pageable pageable) {
        return repository.searchByQuery(searchTerm, pageable);
    }

}