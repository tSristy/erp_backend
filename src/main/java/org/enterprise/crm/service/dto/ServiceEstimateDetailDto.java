package org.enterprise.crm.service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceEstimateDetailDto {
    private Long id;
    private Long productId;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private Boolean isPart;
}
