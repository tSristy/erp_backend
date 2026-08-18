package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(
        name = "business_partner_roles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "partner_id", "role"})
        }
)
@Getter
@Setter
public class BusinessPartnerRole extends AuditableEntity {

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private BusinessPartner partner;

    public enum RoleType {
        CUSTOMER,
        VENDOR,
        EMPLOYEE,
        BANK,
        SHAREHOLDER
    }
}