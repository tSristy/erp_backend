package org.enterprise.inventory.dto;

import lombok.Data;
import java.util.List;
import org.enterprise.common.dto.AuditableDto;

@Data
public class WarehouseDto extends AuditableDto {
    private String code;
    private String name;
    private String address;
    private Long inventoryAccountId;
    private Long cogsAccountId;
    private Long salesRevenueAccountId;
    private Long salesReturnAccountId;
    private Long salesDiscountAccountId;
    private List<LocationDto> locations;
}
