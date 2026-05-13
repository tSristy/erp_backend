package org.enterprise.crm.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "crm_service_parts_req_details")
@Getter
@Setter
public class ServicePartsRequisitionDetail extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id")
    private ServicePartsRequisition requisition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(precision = 18, scale = 2)
    private BigDecimal quantityRequested = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal quantityIssued = BigDecimal.ZERO;
}
