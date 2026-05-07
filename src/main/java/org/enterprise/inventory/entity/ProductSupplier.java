package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "product_suppliers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProductSupplier extends AuditableEntity {

    private String supplierSku;

    private BigDecimal costPrice;

    private Integer leadTimeDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private BusinessPartner supplier;
}