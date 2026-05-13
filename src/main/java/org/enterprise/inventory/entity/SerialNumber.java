package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "inv_serial_numbers")
@Getter
@Setter
public class SerialNumber extends AuditableEntity {

    private String serialNo;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    @Enumerated(EnumType.STRING)
    private SerialStatus status = SerialStatus.IN_STOCK;

    public enum SerialStatus {
        IN_STOCK, ISSUED, RETURNED, SCRAPPED
    }
}
