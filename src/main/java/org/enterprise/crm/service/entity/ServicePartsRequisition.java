package org.enterprise.crm.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.util.List;

@Entity
@Table(name = "crm_service_parts_requisitions")
@Getter
@Setter
public class ServicePartsRequisition extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id")
    private ServiceRequest serviceRequest;

    @Column(name = "requested_by_id")
    private Long requestedById; // Employee or User ID who requested the parts

    @Enumerated(EnumType.STRING)
    private RequisitionStatus status = RequisitionStatus.REQUESTED;

    @OneToMany(mappedBy = "requisition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicePartsRequisitionDetail> details;

    public enum RequisitionStatus {
        REQUESTED, PARTIALLY_ISSUED, ISSUED, CANCELLED
    }
}
