package org.enterprise.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.organization.entity.Company;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "user_companies")
@Getter
@Setter
@SQLDelete(sql = "UPDATE user_companies SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class UserCompany extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_ref_id")
    private Company company;

    private Boolean defaultCompany = false;

    private Boolean active = true;
}