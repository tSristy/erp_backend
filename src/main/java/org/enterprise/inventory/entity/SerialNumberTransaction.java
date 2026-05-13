package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.enums.InventoryTransactionType;

import java.time.LocalDateTime;

@Entity
@Table(name = "inv_serial_number_transactions")
@Getter
@Setter
public class SerialNumberTransaction extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private SerialNumber serialNumber;

    @Enumerated(EnumType.STRING)
    private InventoryTransactionType transactionType;

    private String documentType;

    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    private LocalDateTime transactionDate;
}
