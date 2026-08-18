package org.enterprise.inventory.dto;

import lombok.Data;
import java.util.List;
import org.enterprise.common.dto.AuditableDto;
import org.enterprise.inventory.entity.Location.LocationType;

@Data
public class LocationDto extends AuditableDto {
    private String code;
    private String name;
    private LocationType type;
    private Long warehouseId;
    private Long parentId;
    private List<LocationDto> children;
}
