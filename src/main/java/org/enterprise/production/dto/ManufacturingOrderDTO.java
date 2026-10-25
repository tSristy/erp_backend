package org.enterprise.production.dto;

import lombok.Data;

@Data
public class ManufacturingOrderDTO {
    private Long id;
    private String orderNo;
    private java.time.LocalDate orderDate;
    private Long finishedGoodId;
    private Long bomId;
    private Long productionWarehouseId;
    private java.math.BigDecimal plannedQuantity;
    private java.math.BigDecimal producedQuantity;
    private org.enterprise.production.entity.ManufacturingOrder.OrderStatus status;
    private Long batchId;
    private Long routingId;
    private java.util.List<String> serialNumbers;
    private org.enterprise.production.entity.ManufacturingOrder.QcStatus qcStatus;
    private Long qcInspectorId;
    private java.math.BigDecimal qcPassedQuantity;
    private java.math.BigDecimal qcRejectedQuantity;
    private String qcNotes;
}
