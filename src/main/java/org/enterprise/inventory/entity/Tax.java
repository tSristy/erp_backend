package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "taxes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Tax extends AuditableEntity {

    private String name;

    private BigDecimal rate;
}