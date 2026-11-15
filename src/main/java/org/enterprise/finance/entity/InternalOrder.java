package org.enterprise.finance.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "fin_internal_orders")
@Getter
@Setter
public class InternalOrder extends AuditableEntity {
    private String code;
    private String name;

    @Column(precision = 18, scale = 2)
    private BigDecimal budget = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal utilizedAmount = BigDecimal.ZERO;

    private Boolean budgetControlled = true;

    private Boolean active = true;

    @Transient
    @JsonProperty("description")
    public String getDescription() {
        return this.name;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        if ((this.name == null || this.name.trim().isEmpty()) && description != null) {
            this.name = description;
        }
    }

    @Transient
    @JsonProperty("availableBudget")
    public BigDecimal getAvailableBudget() {
        BigDecimal totalBudget = this.budget != null ? this.budget : BigDecimal.ZERO;
        BigDecimal used = this.utilizedAmount != null ? this.utilizedAmount : BigDecimal.ZERO;
        return totalBudget.subtract(used);
    }
}
