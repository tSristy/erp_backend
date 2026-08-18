package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.enums.WarehouseCategory;
import org.enterprise.inventory.enums.WarehouseType;
import org.enterprise.organization.entity.Branch;
import org.enterprise.finance.entity.Account;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(
        name = "warehouses",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "code"})
        },
        indexes = {
                @Index(name = "idx_wh_company", columnList = "company_id"),
                @Index(name = "idx_wh_branch", columnList = "branch_id")
        }
)
@Getter
@Setter
@SQLDelete(sql = "UPDATE warehouses SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Warehouse extends AuditableEntity {

    private String code;
    private String name;
    private String shortName;

    @Enumerated(EnumType.STRING)
    private WarehouseType type;

    @Enumerated(EnumType.STRING)
    private WarehouseCategory warehouseCategory;

    private Boolean active = true;

    private Boolean defaultWarehouse = false;

    private Boolean allowNegativeStock = false;

    private Boolean batchManaged = false;

    private Boolean serialManaged = false;

    private Boolean binManaged = true;

    private Boolean quarantineEnabled = false;

    private String contactPerson;
    private String email;
    private String phone;

    private String country;
    private String city;
    private String zipCode;

    @Column(columnDefinition = "TEXT")
    private String address;

    private Double latitude;
    private Double longitude;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account inventoryAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account cogsAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account salesRevenueAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account salesReturnAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account salesDiscountAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account wipAccount;

    @OneToMany(mappedBy = "warehouse")
    private List<Location> locations;
}
