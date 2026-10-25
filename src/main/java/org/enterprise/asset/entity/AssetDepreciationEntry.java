package org.enterprise.asset.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fam_asset_depreciation_entries")
@Getter
@Setter
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class AssetDepreciationEntry extends AuditableEntity {

    @Column(nullable = false)
    private String entryNumber;

    @Column(nullable = false)
    private Long assetId;

    private String assetCode;
    private String assetName;

    @Column(nullable = false)
    private LocalDate periodDate;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal depreciationAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal accumulatedDepreciationAfter = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal bookValueAfter = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepreciationStatus status = DepreciationStatus.POSTED;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    public enum DepreciationStatus {
        DRAFT,
        POSTED
    }
}
