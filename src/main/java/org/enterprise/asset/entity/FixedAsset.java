package org.enterprise.asset.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fam_fixed_assets")
@Getter
@Setter
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class FixedAsset extends AuditableEntity {

    @Column(nullable = false)
    private String assetCode;

    @Column(nullable = false)
    private String name;

    private String serialNumber;

    private Long categoryId;
    private String categoryName;

    @Enumerated(EnumType.STRING)
    private AssetCategory.DepreciationMethod depreciationMethod = AssetCategory.DepreciationMethod.STRAIGHT_LINE;

    private Integer usefulLifeYears = 5;

    private LocalDate purchaseDate;
    private LocalDate inServiceDate;

    @Column(precision = 19, scale = 4)
    private BigDecimal purchaseCost = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal salvageValue = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal currentBookValue = BigDecimal.ZERO;

    private Long assignedEmployeeId;
    private String assignedEmployeeName;

    private Long departmentId;
    private String departmentName;

    private String locationName;

    private Long vendorId;
    private String vendorName;

    private Long purchaseOrderId;
    private String purchaseOrderNo;

    private Long goodsReceiptId;
    private String goodsReceiptNo;

    private Long productId;
    private String productName;

    private LocalDate warrantyExpiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status = AssetStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum AssetStatus {
        DRAFT,
        ACTIVE,
        IN_MAINTENANCE,
        DISPOSED,
        WRITTEN_OFF
    }
}
