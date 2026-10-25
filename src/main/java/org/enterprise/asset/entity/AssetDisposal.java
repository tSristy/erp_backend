package org.enterprise.asset.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fam_asset_disposals")
@Getter
@Setter
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class AssetDisposal extends AuditableEntity {

    @Column(nullable = false)
    private String disposalNumber;

    @Column(nullable = false)
    private Long assetId;

    private String assetCode;
    private String assetName;

    @Column(nullable = false)
    private LocalDate disposalDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisposalType disposalType = DisposalType.SALE;

    @Column(precision = 19, scale = 4)
    private BigDecimal saleAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal bookValueAtDisposal = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal gainLossAmount = BigDecimal.ZERO;

    private String buyerName;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisposalStatus status = DisposalStatus.APPROVED;

    public enum DisposalType {
        SALE,
        SCRAP,
        WRITE_OFF,
        DONATION,
        LOST
    }

    public enum DisposalStatus {
        DRAFT,
        APPROVED
    }
}
