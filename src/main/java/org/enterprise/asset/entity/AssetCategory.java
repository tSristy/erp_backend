package org.enterprise.asset.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "fam_asset_categories")
@Getter
@Setter
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class AssetCategory extends AuditableEntity {

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepreciationMethod depreciationMethod = DepreciationMethod.STRAIGHT_LINE;

    @Column(nullable = false)
    private Integer usefulLifeYears = 5;

    @Column(precision = 5, scale = 2)
    private BigDecimal salvagePercent = BigDecimal.ZERO;

    private Long assetAccountId;
    private Long accumulatedDepAccountId;
    private Long depExpenseAccountId;

    private Boolean active = true;

    public enum DepreciationMethod {
        STRAIGHT_LINE,
        DECLINING_BALANCE,
        NO_DEPRECIATION
    }
}
