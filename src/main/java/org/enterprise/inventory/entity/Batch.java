package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDate;

@Entity
@Table(name = "inv_batches")
@Getter
@Setter
public class Batch extends AuditableEntity {

    private String batchNo;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private LocalDate manufactureDate;
    private LocalDate expiryDate;

    private Boolean active = true;
}
