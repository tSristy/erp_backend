package org.enterprise.security.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.TenantEntity;
import org.enterprise.finance.entity.ProfitCenter;

@Entity
@Table(name = "user_profit_centers")
@Getter
@Setter
public class UserProfitCenter extends TenantEntity {

    @ManyToOne
    private User user;

    @ManyToOne
    private ProfitCenter profitCenter;
}
