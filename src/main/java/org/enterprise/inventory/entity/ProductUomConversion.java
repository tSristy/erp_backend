package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "inv_product_uom_conversions")
@Getter
@Setter
public class ProductUomConversion extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private UnitOfMeasure fromUom;

    @ManyToOne(fetch = FetchType.LAZY)
    private UnitOfMeasure toUom;

    @Column(precision = 18, scale = 6)
    private BigDecimal conversionFactor;
}
