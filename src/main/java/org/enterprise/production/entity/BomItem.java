package org.enterprise.production.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "prd_bom_items")
@Getter
@Setter
public class BomItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private BillOfMaterial bom;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product rawMaterial;

    @Column(precision = 18, scale = 6)
    private BigDecimal quantity;
}
