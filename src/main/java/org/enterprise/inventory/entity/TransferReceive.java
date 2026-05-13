package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "inv_transfer_receives")
@Getter
@Setter
public class TransferReceive extends AuditableEntity {

    private String receiveNo;

    private LocalDate receiveDate;

    @Enumerated(EnumType.STRING)
    private ReceiveStatus status = ReceiveStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    private StockTransfer stockTransfer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse destinationWarehouse;

    @OneToMany(mappedBy = "transferReceive", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransferReceiveDetail> details;

    public enum ReceiveStatus {
        DRAFT, COMPLETED, CANCELLED
    }
}
