package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "product_attribute_values")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProductAttributeValue extends AuditableEntity {

    @Column(columnDefinition = "TEXT")
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;
}