package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.finance.entity.Account;
import org.enterprise.inventory.enums.CostingMethod;
import org.enterprise.inventory.enums.ProductType;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "sku"})
        }
)
@Getter
@Setter
public class Product extends AuditableEntity {

    @Column(nullable = false)
    private String sku;

    private String name;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @ManyToOne(fetch = FetchType.LAZY)
    private UnitOfMeasure baseUom;

    private Boolean serialTracked = false;

    private Boolean batchTracked = false;

    private Boolean isBatchManaged = false;

    private Boolean isSerialManaged = false;

    @Enumerated(EnumType.STRING)
    private CostingMethod costingMethod = CostingMethod.AVERAGE;

    private Boolean inventoryItem = true;

    private Boolean purchasable = true;

    private Boolean saleable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account inventoryAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account cogsAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account salesAccount;
}
