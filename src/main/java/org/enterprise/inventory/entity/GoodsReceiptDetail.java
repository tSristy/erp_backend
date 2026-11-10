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

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private GoodsReceipt goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductVariant variant;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "product"})
    private Batch batch;

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("batchId")
    public Long getBatchId() {
        return batch != null ? batch.getId() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("batchNo")
    public String getBatchNo() {
        return batch != null ? batch.getBatchNo() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("manufactureDate")
    public java.time.LocalDate getManufactureDate() {
        return batch != null ? batch.getManufactureDate() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("expiryDate")
    public java.time.LocalDate getExpiryDate() {
        return batch != null ? batch.getExpiryDate() : null;
    }

    @ElementCollection
    @CollectionTable(name = "inv_goods_receipt_detail_serials", joinColumns = @JoinColumn(name = "detail_id"))
    @Column(name = "serial_no")
    private java.util.List<String> serialNumbers = new java.util.ArrayList<>();
}
