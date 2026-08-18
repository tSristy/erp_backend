package org.enterprise.finance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "fin_internal_orders")
@Getter
@Setter
public class InternalOrder extends AuditableEntity {
    private String code;
    private String name;
    private BigDecimal budget;
    private Boolean active = true;
}
