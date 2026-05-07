package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
        name = "product_variants",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "sku"})
        }
)
@Getter
@Setter
@SQLDelete(sql = "UPDATE product_variants SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class ProductVariant extends AuditableEntity {

    private String sku;
    private String barcode;

    @Column(columnDefinition = "JSON")
    private String attributesJson;

    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}