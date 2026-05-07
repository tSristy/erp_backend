package org.enterprise.organization.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.TenantEntity;

@Entity
@Getter @Setter
public class Area extends TenantEntity {

    private String code;
    private String name;

    private String marketType;

    private Integer priorityLevel;

    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    private Territory territory;
}