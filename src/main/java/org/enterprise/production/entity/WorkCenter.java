package org.enterprise.production.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "prd_work_centers")
@Getter
@Setter
public class WorkCenter extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(precision = 18, scale = 2)
    private BigDecimal costPerHour = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal capacityPerHour = BigDecimal.ONE;

    private Boolean active = true;
}
