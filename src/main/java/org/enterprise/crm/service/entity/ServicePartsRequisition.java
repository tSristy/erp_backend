package org.enterprise.crm.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.Warehouse;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crm_service_parts_requisitions")
@Getter
@Setter
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ServicePartsRequisition extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_order_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ServiceOrder serviceOrder;

    @Column(name = "requested_by_id")
    private Long requestedById; // Employee or User ID who requested the parts

    @Enumerated(EnumType.STRING)
    private RequisitionStatus status = RequisitionStatus.REQUESTED;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Warehouse warehouse;

    @OneToMany(mappedBy = "requisition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicePartsRequisitionDetail> details = new ArrayList<>();

    public enum RequisitionStatus {
        REQUESTED, PARTIALLY_ISSUED, ISSUED, CANCELLED
    }

    @Transient
    private Long transientServiceOrderId;

    @com.fasterxml.jackson.annotation.JsonProperty("serviceOrderId")
    public Long getServiceOrderId() {
        return serviceOrder != null ? serviceOrder.getId() : transientServiceOrderId;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("serviceOrderId")
    public void setServiceOrderId(Long serviceOrderId) {
        this.transientServiceOrderId = serviceOrderId;
        if (serviceOrderId != null) {
            if (this.serviceOrder == null) {
                this.serviceOrder = new ServiceOrder();
            }
            this.serviceOrder.setId(serviceOrderId);
        }
    }

    @Transient
    private Long transientWarehouseId;

    @com.fasterxml.jackson.annotation.JsonProperty("warehouseId")
    public Long getWarehouseId() {
        return warehouse != null ? warehouse.getId() : transientWarehouseId;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("warehouseId")
    public void setWarehouseId(Long warehouseId) {
        this.transientWarehouseId = warehouseId;
        if (warehouseId != null) {
            if (this.warehouse == null) {
                this.warehouse = new Warehouse();
            }
            this.warehouse.setId(warehouseId);
        }
    }
}
