package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "inv_transfer_receive_details")
@Getter
@Setter
public class TransferReceiveDetail extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private TransferReceive transferReceive;

    @ManyToOne(fetch = FetchType.LAZY)
    private StockTransferDetail stockTransferDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location destinationLocation;

    @Column(precision = 18, scale = 6)
    private BigDecimal quantity = BigDecimal.ZERO;
}
