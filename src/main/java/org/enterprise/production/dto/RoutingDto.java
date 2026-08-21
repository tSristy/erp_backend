package org.enterprise.production.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoutingDto {
    private Long id;
    private String name;
    private Boolean active;
    private List<RoutingOperationDto> operations;
}
