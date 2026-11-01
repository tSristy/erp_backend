package org.enterprise.crm.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.BusinessPartner;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "crm_opportunities")
@Getter
@Setter
public class Opportunity extends AuditableEntity {

    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lead_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Lead lead;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private BusinessPartner customer;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    private LocalDate expectedCloseDate;

    @Enumerated(EnumType.STRING)
    private OpportunityStage stage = OpportunityStage.PROSPECTING;

    @com.fasterxml.jackson.annotation.JsonGetter("leadId")
    public Long resolveLeadId() {
        return lead != null ? lead.getId() : null;
    }

    @com.fasterxml.jackson.annotation.JsonSetter("leadId")
    public void assignLeadId(Long leadId) {
        if (leadId != null) {
            if (this.lead == null) {
                this.lead = new Lead();
            }
            this.lead.setId(leadId);
        } else {
            this.lead = null;
        }
    }

    @com.fasterxml.jackson.annotation.JsonGetter("leadName")
    public String resolveLeadName() {
        if (lead == null) return null;
        try {
            return lead.resolveFullName();
        } catch (Exception e) {
            return null;
        }
    }

    @com.fasterxml.jackson.annotation.JsonGetter("customerId")
    public Long resolveCustomerId() {
        return customer != null ? customer.getId() : null;
    }

    @com.fasterxml.jackson.annotation.JsonSetter("customerId")
    public void assignCustomerId(Long customerId) {
        if (customerId != null) {
            if (this.customer == null) {
                this.customer = new BusinessPartner();
            }
            this.customer.setId(customerId);
        } else {
            this.customer = null;
        }
    }

    @com.fasterxml.jackson.annotation.JsonGetter("customerName")
    public String resolveCustomerName() {
        if (customer == null) return null;
        try {
            return customer.getName();
        } catch (Exception e) {
            return null;
        }
    }

    public enum OpportunityStage {
        PROSPECTING, QUALIFICATION, PROPOSAL, NEGOTIATION, CLOSED_WON, CLOSED_LOST
    }
}
