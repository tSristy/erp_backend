package org.enterprise.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.util.List;

@Entity
@Table(name = "menus")
@Getter @Setter
@SQLDelete(sql = "UPDATE menus SET deleted = true WHERE id = ?")

@SQLRestriction("deleted = false")
public class Menu extends AuditableEntity {

    private String code;
    private String name;
    private String path;

    private String icon;

    private Integer displayOrder;

    private Boolean visible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    private Module module;

    @ManyToOne(fetch = FetchType.LAZY)
    private Menu parent;

    @OneToMany(mappedBy = "menu")
    private List<Permission> permissions;
}