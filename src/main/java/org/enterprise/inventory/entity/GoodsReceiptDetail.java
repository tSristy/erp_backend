package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "inv_goods_receipt_details")
@Getter
@Setter
public class GoodsReceiptDetail extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private GoodsReceipt goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Column(precision = 18, scale = 6)
    private BigDecimal quantity;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitCost;

    @Column(precision = 18, scale = 2)
    private BigDecimal lineTotal;

    private Long baseDocumentId;

    private Long baseLineId;

    private String baseDocumentType;

    @ManyToOne(fetch = FetchType.LAZY)
    private PurchaseOrderDetail purchaseOrderDetail;
}

