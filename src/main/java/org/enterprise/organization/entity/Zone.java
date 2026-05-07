package org.enterprise.organization.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.TenantEntity;

@Entity
@Getter @Setter
public class Zone extends TenantEntity {

    private String code;
    private String name;

    private String managerName;
    private String contactNo;

    private Boolean active = true;
}
