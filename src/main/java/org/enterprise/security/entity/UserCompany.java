package org.enterprise.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.organization.entity.Company;

@Entity
@Table(name = "user_companies")
@Getter
@Setter
public class UserCompany extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_company_id")
    private Company company;

    private Boolean defaultCompany = false;

    private Boolean active = true;
}