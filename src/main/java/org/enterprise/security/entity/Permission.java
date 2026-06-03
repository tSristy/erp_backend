package org.enterprise.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@SQLDelete(sql = "UPDATE permissions SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Permission extends BaseEntity {

    private Boolean deleted = false;

    @Column(unique = true)
    private String code;

    private String name;

    private String moduleCode;

    private String actionPath;

    private String actionType;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;
}

