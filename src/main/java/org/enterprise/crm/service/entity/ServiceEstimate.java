package org.enterprise.crm.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "crm_service_estimates")
@Getter
@Setter
public class ServiceEstimate extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id")
    private ServiceRequest serviceRequest;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalLaborAmount = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalPartsAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private EstimateStatus status = EstimateStatus.DRAFT;

    @OneToMany(mappedBy = "serviceEstimate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceEstimateDetail> details;

    public enum EstimateStatus {
        DRAFT, SENT, APPROVED, REJECTED
    }
}
