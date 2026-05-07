package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "product_images")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProductImage extends AuditableEntity {

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    private Boolean isPrimary = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}