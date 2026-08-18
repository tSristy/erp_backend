package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "inv_stock_reclassification_details")
@Getter
@Setter
public class StockReclassificationDetail extends AuditableEntity {

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private StockReclassification stockReclassification;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product sourceProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product destinationProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.inventory.entity.Batch sourceBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.inventory.entity.Batch destinationBatch;

    @Column(precision = 18, scale = 6)
    private BigDecimal sourceQuantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 6)
    private BigDecimal destinationQuantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO; // Tracked for journaling purposes

    @Column(precision = 18, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;
}
