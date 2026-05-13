package org.enterprise.crm.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.BusinessPartner;

@Entity
@Table(name = "crm_service_requests")
@Getter
@Setter
public class ServiceRequest extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_product_id")
    private RegisteredProduct registeredProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private BusinessPartner customer;

    @Column(columnDefinition = "TEXT")
    private String issueDescription;

    @Enumerated(EnumType.STRING)
    private ServiceRequestPriority priority = ServiceRequestPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    private ServiceRequestStatus status = ServiceRequestStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "assigned_technician_id")
    private Long assignedTechnicianId;

    public enum ServiceRequestPriority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum ServiceRequestStatus {
        OPEN, DIAGNOSIS, PENDING_ESTIMATE, WAITING_APPROVAL, APPROVED, IN_PROGRESS, WAITING_PARTS, RESOLVED, CLOSED
    }
}
