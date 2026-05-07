package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "inv_stock_reclassifications")
@Getter
@Setter
public class StockReclassification extends AuditableEntity {

    private String reclassNo;

    private LocalDate reclassDate;

    @Enumerated(EnumType.STRING)
    private ReclassStatus status = ReclassStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    @OneToMany(mappedBy = "stockReclassification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockReclassificationDetail> details;

    public enum ReclassStatus {
        DRAFT, COMPLETED, CANCELLED
    }
}
