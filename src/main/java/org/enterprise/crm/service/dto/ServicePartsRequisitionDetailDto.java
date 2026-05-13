package org.enterprise.crm.service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServicePartsRequisitionDetailDto {
    private Long id;
    private Long productId;
    private BigDecimal quantityRequested;
    private BigDecimal quantityIssued;
}
