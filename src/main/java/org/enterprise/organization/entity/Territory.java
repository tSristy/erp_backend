package org.enterprise.organization.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.TenantEntity;

@Entity
@Getter @Setter
public class Territory extends TenantEntity {

    private String code;
    private String name;

    private String salesType;

    private String managerName;
    private String contactNo;

    private Double salesTarget;

    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    private Zone zone;
}