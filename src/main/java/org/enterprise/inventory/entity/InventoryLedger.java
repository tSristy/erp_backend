package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.enums.InventoryTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inv_inventory_ledger")
@Getter
@Setter
public class InventoryLedger extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    @Enumerated(EnumType.STRING)
    private InventoryTransactionType transactionType;


    private String documentType;

    private Long documentId;

    private LocalDateTime transactionDate;

    private BigDecimal qtyIn = BigDecimal.ZERO;

    private BigDecimal qtyOut = BigDecimal.ZERO;

    private BigDecimal unitCost = BigDecimal.ZERO;

    private BigDecimal totalCost = BigDecimal.ZERO;

    private BigDecimal balanceQty = BigDecimal.ZERO;

    private BigDecimal balanceCost = BigDecimal.ZERO;
}