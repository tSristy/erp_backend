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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private BusinessPartner customer;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    private LocalDate expectedCloseDate;

    @Enumerated(EnumType.STRING)
    private OpportunityStage stage = OpportunityStage.PROSPECTING;

    public enum OpportunityStage {
        PROSPECTING, QUALIFICATION, PROPOSAL, NEGOTIATION, CLOSED_WON, CLOSED_LOST
    }
}
