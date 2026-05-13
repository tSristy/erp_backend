package org.enterprise.crm.service.dto;

import lombok.Data;
import org.enterprise.crm.service.entity.ServiceEstimate;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ServiceEstimateDto {
    private Long id;
    private Long serviceRequestId;
    private BigDecimal totalLaborAmount;
    private BigDecimal totalPartsAmount;
    private ServiceEstimate.EstimateStatus status;
    private List<ServiceEstimateDetailDto> details;
}
