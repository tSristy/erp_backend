package org.enterprise.crm.sales.dto;

import lombok.Data;
import org.enterprise.crm.sales.entity.Opportunity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OpportunityDto {
    private Long id;
    private String name;
    private Long leadId;
    private Long customerId;
    private BigDecimal amount;
    private LocalDate expectedCloseDate;
    private Opportunity.OpportunityStage stage;
}
