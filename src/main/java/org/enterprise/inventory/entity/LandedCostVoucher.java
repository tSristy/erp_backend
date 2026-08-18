package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "inv_landed_cost_vouchers")
@Getter
@Setter
public class LandedCostVoucher extends AuditableEntity {

    @Column(unique = true)
    private String voucherNo;

    private LocalDate postingDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private GoodsReceipt goodsReceipt;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalSecondaryCost = BigDecimal.ZERO;

    private Long workflowInstanceId;

    @Enumerated(EnumType.STRING)
    private LandedCostStatus status = LandedCostStatus.DRAFT;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LandedCostDetail> details;

    public enum LandedCostStatus {
        DRAFT, POSTED, CANCELLED
    }
}
