package org.enterprise.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@SQLDelete(sql = "UPDATE roles SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Role extends AuditableEntity {

    @Column(nullable = false)
    private String code;

    private String name;

    @OneToMany(
            mappedBy = "role",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<RolePermission> rolePermissions;
}