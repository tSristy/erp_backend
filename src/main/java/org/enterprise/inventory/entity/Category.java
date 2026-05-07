package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "categories")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Category extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
}