package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.finance.entity.Account;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
        name = "business_partners",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "code"})
        },
        indexes = {
                @Index(name = "idx_bp_name", columnList = "name")
        }
)
@Getter
@Setter
@SQLDelete(sql = "UPDATE business_partners SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class BusinessPartner extends AuditableEntity {

    private String code;

    private String name;

    @Enumerated(EnumType.STRING)
    private PartnerType partnerType;

    private String email;
    private String phone;
    private String mobile;

    private String website;

    private Boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account grnClearingAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account accountsReceivableAccount;

    public enum PartnerType {
        INDIVIDUAL, COMPANY
    }
}