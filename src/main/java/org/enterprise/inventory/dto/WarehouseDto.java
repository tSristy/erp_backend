package org.enterprise.inventory.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import org.enterprise.common.dto.AuditableDto;
import org.enterprise.inventory.enums.WarehouseCategory;
import org.enterprise.inventory.enums.WarehouseType;

@Data
public class WarehouseDto extends AuditableDto {
    private String code;
    private String name;
    private String shortName;
    private WarehouseType type;
    private WarehouseCategory warehouseCategory;
    private Integer displayOrder;
    private Boolean active;
    private Boolean defaultWarehouse;
    private Boolean allowNegativeStock;
    private Boolean batchManaged;
    private Boolean serialManaged;
    private Boolean binManaged;
    private Boolean quarantineEnabled;
    private String contactPerson;
    private String email;
    private String phone;
    private String country;
    private String city;
    private String zipCode;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String remarks;

    private Long branchId;
    private String branchName;
    private Object branch;

    private Long inventoryAccountId;
    private String inventoryAccountName;
    private Object inventoryAccount;

    private Long cogsAccountId;
    private String cogsAccountName;
    private Object cogsAccount;

    private Long salesRevenueAccountId;
    private String salesRevenueAccountName;
    private Object salesRevenueAccount;

    private Long salesReturnAccountId;
    private String salesReturnAccountName;
    private Object salesReturnAccount;

    private Long salesDiscountAccountId;
    private String salesDiscountAccountName;
    private Object salesDiscountAccount;

    private Long wipAccountId;
    private String wipAccountName;
    private Object wipAccount;

    private List<LocationDto> locations;
}

