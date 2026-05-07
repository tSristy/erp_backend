package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.enums.GoodsReceiptStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "inv_goods_receipts")
@Getter
@Setter
public class GoodsReceipt extends AuditableEntity {

    private String grnNo;

    private LocalDate grnDate;

    @Enumerated(EnumType.STRING)
    private GoodsReceiptStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    private PurchaseOrder purchaseOrder;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiptDetail> details;

    @Enumerated(EnumType.STRING)
    private ReceiptType receiptType = ReceiptType.INBOUND_RECEIPT;

    public enum ReceiptType {
        INBOUND_RECEIPT, OUTBOUND_RETURN
    }
}
