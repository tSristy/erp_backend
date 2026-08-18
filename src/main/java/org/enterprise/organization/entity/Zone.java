package org.enterprise.organization.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.TenantEntity;

@Entity
@Getter @Setter
public class Zone extends TenantEntity {

    private String code;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.hr.entity.Employee regionalManager;

    private Boolean active = true;
}
