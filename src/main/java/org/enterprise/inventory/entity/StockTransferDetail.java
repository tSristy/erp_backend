package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "inv_stock_transfer_details")
@Getter
@Setter
public class StockTransferDetail extends AuditableEntity {

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private StockTransfer stockTransfer;

    @ManyToOne(fetch = FetchType.LAZY)
    private TransferOrderDetail transferOrderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location sourceLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location destinationLocation;

    @Column(precision = 18, scale = 6)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 6)
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO; // Tracked for journaling purposes

    @Column(precision = 18, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    private Batch batch;

    @ElementCollection
    @CollectionTable(name = "inv_stock_transfer_detail_serials", joinColumns = @JoinColumn(name = "detail_id"))
    @Column(name = "serial_no")
    private java.util.List<String> serialNumbers = new java.util.ArrayList<>();
}
