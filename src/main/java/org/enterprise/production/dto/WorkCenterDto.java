package org.enterprise.production.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WorkCenterDto {
    private Long id;
    private String name;
    private String code;
    private BigDecimal costPerHour;
    private BigDecimal capacityPerHour;
    private Boolean active;
}
