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

    @OneToMany(mappedBy = "partner", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<BusinessPartnerRole> roles = new java.util.ArrayList<>();

    @Transient
    private String role;

    public String getRole() {
        if (roles != null && !roles.isEmpty()) {
            boolean hasCust = roles.stream().anyMatch(r -> r.getRole() == BusinessPartnerRole.RoleType.CUSTOMER);
            boolean hasVend = roles.stream().anyMatch(r -> r.getRole() == BusinessPartnerRole.RoleType.VENDOR);
            if (hasCust && hasVend) return "BOTH";
            return roles.get(0).getRole().name();
        }
        return role;
    }

    public void setRole(String role) {
        this.role = role;
        if (role != null) {
            if (this.roles == null) {
                this.roles = new java.util.ArrayList<>();
            } else {
                this.roles.clear();
            }
            
            if (role.equals("BOTH")) {
                BusinessPartnerRole c = new BusinessPartnerRole();
                c.setRole(BusinessPartnerRole.RoleType.CUSTOMER);
                c.setPartner(this);
                this.roles.add(c);

                BusinessPartnerRole v = new BusinessPartnerRole();
                v.setRole(BusinessPartnerRole.RoleType.VENDOR);
                v.setPartner(this);
                this.roles.add(v);
            } else {
                try {
                    BusinessPartnerRole r = new BusinessPartnerRole();
                    r.setRole(BusinessPartnerRole.RoleType.valueOf(role));
                    r.setPartner(this);
                    this.roles.add(r);
                } catch (IllegalArgumentException e) {
                    // Ignore invalid roles
                }
            }
        }
    }

    @OneToOne(mappedBy = "partner", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private CustomerDetail customerDetail;

    @OneToOne(mappedBy = "partner", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private VendorDetail vendorDetail;

    @OneToOne(mappedBy = "partner", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ShareholderDetail shareholderDetail;





    public enum PartnerType {
        INDIVIDUAL, COMPANY
    }
}