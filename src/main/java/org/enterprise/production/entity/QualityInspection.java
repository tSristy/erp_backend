package org.enterprise.production.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "prd_quality_inspections")
@Getter
@Setter
public class QualityInspection extends AuditableEntity {

    @Column(unique = true)
    private String inspectionNo;

    private LocalDate inspectionDate = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturing_order_id", nullable = false)
    private ManufacturingOrder manufacturingOrder;

    private Long inspectorId;

    @Column(precision = 18, scale = 6)
    private BigDecimal inspectedQuantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 6)
    private BigDecimal passedQuantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 6)
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 6)
    private BigDecimal reworkQuantity = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private ManufacturingOrder.QcStatus status = ManufacturingOrder.QcStatus.PENDING;

    private String defectReason;

    @Column(columnDefinition = "TEXT")
    private String remarks;
}
