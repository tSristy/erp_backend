package org.enterprise.production.dto;

import lombok.Data;
import org.enterprise.production.entity.ManufacturingOrder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class QualityInspectionDTO {
    private Long id;
    private String inspectionNo;
    private LocalDate inspectionDate;
    private Long manufacturingOrderId;
    private String manufacturingOrderNo;
    private Long finishedGoodId;
    private String finishedGoodName;
    private BigDecimal plannedQuantity;
    private Long inspectorId;
    private BigDecimal inspectedQuantity;
    private BigDecimal passedQuantity;
    private BigDecimal rejectedQuantity;
    private BigDecimal reworkQuantity;
    private ManufacturingOrder.QcStatus status;
    private String defectReason;
    private String remarks;
}
