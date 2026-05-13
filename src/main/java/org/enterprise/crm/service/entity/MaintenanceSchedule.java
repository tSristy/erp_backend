package org.enterprise.crm.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDate;

@Entity
@Table(name = "crm_maintenance_schedules")
@Getter
@Setter
public class MaintenanceSchedule extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_product_id")
    private RegisteredProduct registeredProduct;

    private LocalDate scheduledDate;

    private String maintenanceType;

    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum MaintenanceStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
