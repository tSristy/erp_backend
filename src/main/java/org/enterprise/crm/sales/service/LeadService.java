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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class LeadService extends BaseService<Lead, Long> {

    private final LeadRepository repository;
    private final BusinessPartnerRepository businessPartnerRepository;
    private final OpportunityRepository opportunityRepository;

    public LeadService(
            LeadRepository repository,
            BusinessPartnerRepository businessPartnerRepository,
            OpportunityRepository opportunityRepository) {
        super(repository);
        this.repository = repository;
        this.businessPartnerRepository = businessPartnerRepository;
        this.opportunityRepository = opportunityRepository;
    }

    @Override
    @Transactional
    public Lead save(Lead lead) {
        if (lead.getCompanyId() == null) {
            lead.setCompanyId(TenantContext.getCompanyId());
        }
        Lead saved = super.save(lead);
        if (saved.getStatus() == Lead.LeadStatus.CONVERTED
                && (saved.getConvertedCustomerId() == null || saved.getConvertedOpportunityId() == null)) {
            return convertLead(saved.getId(), null, null, null);
        }
        return saved;
    }

    @Transactional
    public Lead convertLead(Long leadId, String opportunityName, BigDecimal expectedAmount, LocalDate expectedCloseDate) {
        Lead lead = repository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found with ID: " + leadId));

        Long companyId = lead.getCompanyId() != null ? lead.getCompanyId() : TenantContext.getCompanyId();

        // 1. Resolve or create Customer (BusinessPartner)
        BusinessPartner customer = null;
        if (lead.getConvertedCustomerId() != null) {
            customer = businessPartnerRepository.findById(lead.getConvertedCustomerId()).orElse(null);
        }
        if (customer == null) {
            BusinessPartner bp = new BusinessPartner();
            bp.setCompanyId(companyId);
            String fullName = lead.resolveFullName();
            boolean hasCompany = lead.getCompany() != null && !lead.getCompany().trim().isEmpty();
            bp.setName(hasCompany ? lead.getCompany().trim() + " (" + fullName + ")" : fullName);
            bp.setCode("CUST-L" + lead.getId() + "-" + (System.currentTimeMillis() % 10000));
            bp.setEmail(lead.getEmail());
            bp.setPhone(lead.getPhone());
            bp.setMobile(lead.getPhone());
            bp.setPartnerType(hasCompany ? BusinessPartner.PartnerType.COMPANY : BusinessPartner.PartnerType.INDIVIDUAL);
            bp.setActive(true);
            bp.setRole("CUSTOMER");
            bp.setRemarks("Converted from CRM Lead #" + lead.getId() + (lead.getSource() != null ? " (Source: " + lead.getSource() + ")" : ""));
            customer = businessPartnerRepository.save(bp);
        }

        // 2. Resolve or create Opportunity
        Opportunity opportunity = null;
        if (lead.getConvertedOpportunityId() != null) {
            opportunity = opportunityRepository.findById(lead.getConvertedOpportunityId()).orElse(null);
        }
        if (opportunity == null) {
            List<Opportunity> existingForLead = opportunityRepository.findByLeadId(lead.getId());
            if (!existingForLead.isEmpty()) {
                opportunity = existingForLead.get(0);
                if (opportunity.getCustomer() == null) {
                    opportunity.setCustomer(customer);
                    opportunity = opportunityRepository.save(opportunity);
                }
            } else {
                Opportunity opp = new Opportunity();
                opp.setCompanyId(companyId);
                opp.setLead(lead);
                opp.setCustomer(customer);
                String defaultOppName = (lead.getCompany() != null && !lead.getCompany().trim().isEmpty())
                        ? lead.getCompany().trim() + " - Sales Opportunity"
                        : lead.resolveFullName() + " - Sales Opportunity";
                opp.setName(opportunityName != null && !opportunityName.trim().isEmpty() ? opportunityName.trim() : defaultOppName);
                opp.setAmount(expectedAmount != null ? expectedAmount : BigDecimal.ZERO);
                opp.setExpectedCloseDate(expectedCloseDate != null ? expectedCloseDate : LocalDate.now().plusDays(30));
                opp.setStage(Opportunity.OpportunityStage.QUALIFICATION);
                opportunity = opportunityRepository.save(opp);
            }
        }

        lead.setStatus(Lead.LeadStatus.CONVERTED);
        lead.setConvertedCustomerId(customer.getId());
        lead.setConvertedOpportunityId(opportunity.getId());
        return repository.save(lead);
    }

    public Page<Lead> searchLeads(Long companyId, String searchTerm, Pageable pageable) {
        return repository.searchByQuery(searchTerm, pageable);
    }

}