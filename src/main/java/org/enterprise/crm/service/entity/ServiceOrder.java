package org.enterprise.crm.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "crm_service_orders")
@Getter
@Setter
public class ServiceOrder extends AuditableEntity {

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id", nullable = false)
    private ServiceRequest serviceRequest;

    @Column(name = "assigned_technician_id")
    private Long assignedTechnicianId;

    private LocalDate scheduledDate;

    private LocalTime scheduledTime;

    @Enumerated(EnumType.STRING)
    private ServiceOrderStatus status = ServiceOrderStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String technicianNotes;

    // Quality Control (QC) Fields
    @Enumerated(EnumType.STRING)
    private QCStatus qcStatus = QCStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String qcNotes;

    @Column(name = "qc_inspector_id")
    private Long qcInspectorId;

    public enum ServiceOrderStatus {
        OPEN, DIAGNOSED, IN_PROGRESS, WAITING_PARTS, RESOLVED
    }

    public enum QCStatus {
        PENDING, PASSED, FAILED
    }
}
