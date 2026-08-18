package org.enterprise.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.finance.entity.Account;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "acc_cost_heads")
@Getter
@Setter
public class CostHead extends AuditableEntity {

    private String code;

    private String name;

    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;
}