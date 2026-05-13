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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id")
    private Opportunity opportunity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    private LocalDateTime interactionDate;

    @Enumerated(EnumType.STRING)
    private InteractionType type;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum InteractionType {
        CALL, EMAIL, MEETING, NOTE
    }
}
