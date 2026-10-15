package org.enterprise.crm.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crm_service_estimates")
@Getter
@Setter
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceEstimate extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ServiceRequest serviceRequest;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalLaborAmount = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalPartsAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private EstimateStatus status = EstimateStatus.DRAFT;

    @OneToMany(mappedBy = "serviceEstimate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceEstimateDetail> details = new ArrayList<>();

    public enum EstimateStatus {
        DRAFT, SENT, APPROVED, REJECTED
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
    @com.fasterxml.jackson.annotation.JsonProperty("serviceOrderId")
    public Long getServiceOrderId() {
        return getServiceRequestId();
    }

    @com.fasterxml.jackson.annotation.JsonProperty("serviceOrderId")
    public void setServiceOrderId(Long serviceOrderId) {
        if (this.transientServiceRequestId == null) {
            setServiceRequestId(serviceOrderId);
        }
    }
}
