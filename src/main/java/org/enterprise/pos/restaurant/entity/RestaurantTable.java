package org.enterprise.pos.restaurant.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.Warehouse;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(
        name = "pos_restaurant_tables",
        indexes = {
                @Index(name = "idx_restaurant_table_wh", columnList = "company_id, warehouse_id")
        }
)
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@SQLDelete(sql = "UPDATE pos_restaurant_tables SET deleted = true WHERE id = ?")
public class RestaurantTable extends AuditableEntity {

    @Column(name = "table_number", nullable = false)
    private String tableNumber; // e.g. "T-01", "Table 1"

    private String name; // e.g. "Window Table 1"

    private Integer capacity = 4; // number of seats

    private String section; // e.g. "Main Dining", "Window", "Patio", "Rooftop", "AC Room"

    @Enumerated(EnumType.STRING)
    private TableStatus status = TableStatus.AVAILABLE;

    private Boolean active = true;

    private Integer displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    @JsonIgnore
    private Warehouse warehouse;

    public enum TableStatus {
        AVAILABLE, OCCUPIED, RESERVED, OUT_OF_SERVICE
    }

    @Transient
    private Long transientWarehouseId;

    @JsonProperty("warehouseId")
    public Long getWarehouseId() {
        return warehouse != null ? warehouse.getId() : transientWarehouseId;
    }

    @JsonProperty("warehouseId")
    public void setWarehouseId(Long warehouseId) {
        this.transientWarehouseId = warehouseId;
        if (warehouseId != null) {
            if (this.warehouse == null) {
                this.warehouse = new Warehouse();
            }
            this.warehouse.setId(warehouseId);
        }
    }

    @JsonProperty("warehouseName")
    public String getWarehouseName() {
        if (warehouse != null) {
            try {
                return warehouse.getName();
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}
