package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "inv_stock_transfers")
@Getter
@Setter
public class StockTransfer extends AuditableEntity {

    private String transferNo;

    private LocalDate transferDate;

    @Enumerated(EnumType.STRING)
    private TransferStatus status = TransferStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    private TransferOrder transferOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse sourceWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse destinationWarehouse;

    @OneToMany(mappedBy = "stockTransfer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockTransferDetail> details;

    public enum TransferStatus {
        DRAFT, COMPLETED, CANCELLED
    }
}
