package org.enterprise.production.dto;

import lombok.Data;

@Data
public class BillOfMaterialDTO {
    private Long id;
    private Long finishedGoodId;
    private java.math.BigDecimal baseQuantity;
    private Boolean active;
    private java.util.List<BomItemDTO> items;
}
