package org.enterprise.production.dto;

import lombok.Data;

@Data
public class BomItemDTO {
    private Long id;
    private Long bomId;
    private Long rawMaterialId;
    private java.math.BigDecimal quantity;
}
