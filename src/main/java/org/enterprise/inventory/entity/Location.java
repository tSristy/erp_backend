package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.util.List;

@Entity
@Table(
        name = "locations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "warehouse_id", "code"})
        }
)
@Getter
@Setter
@SQLDelete(sql = "UPDATE locations SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Location extends AuditableEntity {

    private String code;
    private String name;

    //private String type;
    // ZONE, AISLE, RACK, SHELF, BIN
    @Enumerated(EnumType.STRING)
    private LocationType type;

    private Boolean active = true;

    private Boolean pickable = true;

    private Boolean receivable = true;

    private Boolean dispatchable = true;

    private Boolean quarantine = false;

    private Boolean damageLocation = false;

    private Boolean returnLocation = false;

    private Integer sequenceNo;

    private Double maxWeight;

    private Double maxVolume;

    private Double currentCapacity;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("locations")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"children", "warehouse"})
    private Location parent;

    @OneToMany(mappedBy = "parent")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("parent")
    private List<Location> children;

    public enum LocationType {
        ZONE, AISLE, RACK, SHELF, BIN
    }
}