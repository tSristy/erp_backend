package org.enterprise.crm.sales.dto;

import lombok.Data;
import org.enterprise.crm.sales.entity.Interaction;

import java.time.LocalDateTime;

@Data
public class InteractionDto {
    private Long id;
    private Long opportunityId;
    private Long leadId;
    private LocalDateTime interactionDate;
    private Interaction.InteractionType type;
    private String notes;
}
