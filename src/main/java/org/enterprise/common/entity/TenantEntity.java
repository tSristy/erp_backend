package org.enterprise.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "companyId", type = Long.class)
)

@Filter(
        name = "tenantFilter",
        condition = "company_id = :companyId"
)
@Getter
@Setter
public abstract class TenantEntity extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;
}