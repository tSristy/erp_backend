package org.enterprise.hr.entity;

import org.enterprise.common.entity.AuditableEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hr_tax_slab")
@Getter
@Setter
public class TaxSlab extends AuditableEntity {

    private Double fromAmount;
    private Double toAmount;
    private Double percentage;

}
