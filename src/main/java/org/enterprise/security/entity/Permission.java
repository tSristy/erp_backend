package org.enterprise.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission extends AuditableEntity {

    @Column(unique = true)
    private String code;

    private String name;

    private String moduleCode;

    private String actionType;
    // CREATE
    // READ
    // UPDATE
    // DELETE
    // APPROVE
    // CANCEL
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;
}

