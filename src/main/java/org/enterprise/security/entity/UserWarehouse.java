package org.enterprise.security.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.TenantEntity;
import org.enterprise.inventory.entity.Warehouse;

@Entity
@Table(name = "user_warehouses")
@Getter
@Setter
public class UserWarehouse extends TenantEntity {

    @ManyToOne
    private User user;

    @ManyToOne
    private Warehouse warehouse;
}
