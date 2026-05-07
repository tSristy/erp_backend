package org.enterprise.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.Warehouse;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "sal_delivery_notes")
@Getter
@Setter
public class DeliveryNote extends AuditableEntity {

    private String deliveryNo;

    private LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status = DeliveryStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner customer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    @OneToMany(mappedBy = "deliveryNote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryNoteDetail> details;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType = DeliveryType.OUTBOUND;

    public enum DeliveryStatus {
        DRAFT, SHIPPED, DELIVERED, CANCELLED
    }

    public enum DeliveryType {
        OUTBOUND, INBOUND_RETURN
    }
}
