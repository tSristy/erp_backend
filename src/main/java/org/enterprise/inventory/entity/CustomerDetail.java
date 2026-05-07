package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "customer_details")
@Getter
@Setter
public class CustomerDetail extends AuditableEntity {

    private String customerGroup;

    private String priceList;

    private Boolean allowPartialDelivery = true;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner partner;
}