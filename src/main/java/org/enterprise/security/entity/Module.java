package org.enterprise.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "modules")
@Getter @Setter
@SQLDelete(sql = "UPDATE modules SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Module extends AuditableEntity {

    private String code;        // INVENTORY, FINANCE
    private String name;
    private String icon;
    private String route;

    private Integer displayOrder;

    private Boolean active = true;
    private Boolean installed = true;
    private Boolean visibleInLauncher = true;

    private String category; // CORE, ERP, CRM

    @JsonIgnore
    @OneToMany(mappedBy = "module")
    private List<Menu> menus;
}