package org.enterprise.production.dto;

import lombok.Data;

@Data
public class RoutingOperationDto {
    private Long id;
    private Integer sequence;
    private String operationName;
    private Long workCenterId;
    private Integer durationMinutes;
}
