package org.enterprise.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "product_uom")
@Getter
@Setter
public class UnitOfMeasure extends AuditableEntity {

    private String code;

    private String name;

    private Boolean active = true;
}

