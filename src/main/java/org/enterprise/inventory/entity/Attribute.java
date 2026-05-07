package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "attributes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Attribute extends AuditableEntity {

    @Column(nullable = false)
    private String name;
}
