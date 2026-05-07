package org.enterprise.production.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.Product;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "prd_bill_of_materials")
@Getter
@Setter
public class BillOfMaterial extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Product finishedGood;

    @Column(precision = 18, scale = 6)
    private BigDecimal baseQuantity = BigDecimal.ONE;

    private Boolean active = true;

    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BomItem> items;
}
