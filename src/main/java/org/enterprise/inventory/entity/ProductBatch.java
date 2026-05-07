package org.enterprise.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDate;

@Entity
@Table(name = "inv_product_batches")
@Getter
@Setter
public class ProductBatch extends AuditableEntity {

    private String batchNo;

    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;
}
