package org.enterprise.crm.sales.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "crm_leads")
@Getter
@Setter
public class Lead extends AuditableEntity {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String company;
    private String source;
    
    @Enumerated(EnumType.STRING)
    private LeadStatus status = LeadStatus.NEW;

    public enum LeadStatus {
        NEW, CONTACTED, QUALIFIED, UNQUALIFIED, CONVERTED
    }
}
