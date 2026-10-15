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
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @Transient
    private Long transientRegisteredProductId;

    @com.fasterxml.jackson.annotation.JsonProperty("registeredProductId")
    public Long getRegisteredProductId() {
        return registeredProduct != null ? registeredProduct.getId() : transientRegisteredProductId;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("registeredProductId")
    public void setRegisteredProductId(Long registeredProductId) {
        this.transientRegisteredProductId = registeredProductId;
        if (registeredProductId != null) {
            if (this.registeredProduct == null) {
                this.registeredProduct = new RegisteredProduct();
            }
            this.registeredProduct.setId(registeredProductId);
        } else {
            this.registeredProduct = null;
        }
    }
}
