package org.enterprise.crm.service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crm_technician_stock_movements")
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TechnicianStockMovement extends AuditableEntity {

    @Column(name = "movement_no", nullable = false)
    private String movementNo;

    @Column(name = "movement_date")
    private String movementDate;

    @Column(name = "movement_type", nullable = false)
    private String movementType; // KIT_ISSUE or KIT_RETURN

    @Column(name = "technician_id", nullable = false)
    private Long technicianId;

    @Column(name = "technician_name")
    private String technicianName;

    @Column(name = "main_warehouse_id", nullable = false)
    private Long mainWarehouseId;

    @Column(name = "main_warehouse_name")
    private String mainWarehouseName;

    @Column(name = "field_warehouse_id", nullable = false)
    private Long fieldWarehouseId;

    @Column(name = "field_warehouse_name")
    private String fieldWarehouseName;

    @Column(name = "stock_transfer_id")
    private Long stockTransferId;

    @Column(name = "stock_transfer_no")
    private String stockTransferNo;

    @Column(name = "status")
    private String status = "COMPLETED";

    @Column(name = "remarks", length = 500)
    private String remarks;

    @OneToMany(mappedBy = "movement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TechnicianStockMovementLine> lines = new ArrayList<>();
}
