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

    @Column(precision = 18, scale = 6)
    private BigDecimal invoicedQuantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 6)
    private BigDecimal returnedQuantity = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitCost;

    @Column(precision = 18, scale = 2)
    private BigDecimal lineTotal;

    private Long baseDocumentId;

    private Long baseLineId;

    private String baseDocumentType;

    @ManyToOne(fetch = FetchType.LAZY)
    private PurchaseOrderDetail purchaseOrderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    private Batch batch;

    @ElementCollection
    @CollectionTable(name = "inv_goods_receipt_detail_serials", joinColumns = @JoinColumn(name = "detail_id"))
    @Column(name = "serial_no")
    private java.util.List<String> serialNumbers = new java.util.ArrayList<>();
}
