package org.enterprise.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role extends AuditableEntity {

    @Column(unique = true)
    private String code;

    private String name;

    private String description;

    private Boolean active = true;

    @OneToMany(mappedBy = "role",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<RolePermission> rolePermissions;
}