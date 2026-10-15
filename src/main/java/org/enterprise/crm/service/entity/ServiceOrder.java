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
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceOrder extends AuditableEntity {

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @Transient
    private Long transientServiceRequestId;

    @com.fasterxml.jackson.annotation.JsonProperty("serviceRequestId")
    public Long getServiceRequestId() {
        return serviceRequest != null ? serviceRequest.getId() : transientServiceRequestId;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("serviceRequestId")
    public void setServiceRequestId(Long serviceRequestId) {
        this.transientServiceRequestId = serviceRequestId;
        if (serviceRequestId != null) {
            if (this.serviceRequest == null) {
                this.serviceRequest = new ServiceRequest();
            }
            this.serviceRequest.setId(serviceRequestId);
        }
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("serviceRequestNumber")
    public String getServiceRequestNumber() {
        return serviceRequest != null ? serviceRequest.getRequestNumber() : null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("serviceRequestNumber")
    public void setServiceRequestNumber(String serviceRequestNumber) {
        // Accept for deserialization
    }
}
