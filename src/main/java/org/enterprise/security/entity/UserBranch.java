package org.enterprise.security.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.TenantEntity;
import org.enterprise.organization.entity.Branch;

@Entity
@Table(name = "user_branches")
@Getter @Setter
public class UserBranch extends TenantEntity {

    @ManyToOne
    private User user;

    @ManyToOne
    private Branch branch;

    private Boolean isDefault = false;
}