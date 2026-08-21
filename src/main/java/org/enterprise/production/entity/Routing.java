package org.enterprise.production.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.util.List;

@Entity
@Table(name = "prd_routings")
@Getter
@Setter
public class Routing extends AuditableEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private Boolean active = true;

    @OneToMany(mappedBy = "routing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutingOperation> operations;
}
