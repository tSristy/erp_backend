package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "inv_transfer_orders")
@Getter
@Setter
public class TransferOrder extends AuditableEntity {

    private String orderNo;

    private LocalDate orderDate;

    @Enumerated(EnumType.STRING)
    private TransferOrderStatus status = TransferOrderStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse sourceWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse destinationWarehouse;

    @OneToMany(mappedBy = "transferOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransferOrderDetail> details;

    public enum TransferOrderStatus {
        DRAFT, PENDING_APPROVAL, APPROVED, PARTIAL_TRANSFER, TRANSFERRED, CLOSED, CANCELLED
    }
}
