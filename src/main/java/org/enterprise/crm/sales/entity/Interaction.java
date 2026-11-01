package org.enterprise.crm.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "crm_interactions")
@Getter
@Setter
public class Interaction extends AuditableEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "opportunity_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Opportunity opportunity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lead_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Lead lead;

    private LocalDateTime interactionDate;

    @Enumerated(EnumType.STRING)
    private InteractionType type;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @com.fasterxml.jackson.annotation.JsonGetter("opportunityId")
    public Long resolveOpportunityId() {
        return opportunity != null ? opportunity.getId() : null;
    }

    @com.fasterxml.jackson.annotation.JsonSetter("opportunityId")
    public void assignOpportunityId(Long opportunityId) {
        if (opportunityId != null) {
            if (this.opportunity == null) {
                this.opportunity = new Opportunity();
            }
            this.opportunity.setId(opportunityId);
        } else {
            this.opportunity = null;
        }
    }

    @com.fasterxml.jackson.annotation.JsonGetter("opportunityName")
    public String resolveOpportunityName() {
        if (opportunity == null) return null;
        try {
            return opportunity.getName();
        } catch (Exception e) {
            return null;
        }
    }

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

    public enum InteractionType {
        CALL, EMAIL, MEETING, NOTE
    }
}
