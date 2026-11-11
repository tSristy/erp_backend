package org.enterprise.pos.retail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.Batch;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.ProductVariant;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.enterprise.common.event.PosLineItem;
import org.enterprise.common.event.PosLineItemDiscount;

@Entity
@Table(name = "pos_retail_transaction_details")
@Getter
@Setter
public class RetailTransactionDetail extends AuditableEntity implements PosLineItem {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private RetailTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_transaction_detail_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private RetailTransactionDetail referenceTransactionDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Batch batch;

    @Column(length = 100)
    private String batchNo;

    @Column(length = 100)
    private String serialNo;

    @ElementCollection
    @CollectionTable(name = "pos_retail_transaction_detail_serials", joinColumns = @JoinColumn(name = "detail_id"))
    @Column(name = "serial_no")
    private List<String> serialNumbers = new ArrayList<>();

    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal quantity;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "transactionDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RetailTransactionDetailDiscount> discounts = new ArrayList<>();

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal lineTotal;

    public void addDiscount(RetailTransactionDetailDiscount discount) {
        discounts.add(discount);
        discount.setTransactionDetail(this);
    }

    public void addSerial(String serial) {
        if (serial != null && !serial.trim().isEmpty()) {
            serialNumbers.add(serial.trim());
            if (this.serialNo == null || this.serialNo.trim().isEmpty()) {
                this.serialNo = serial.trim();
            }
        }
    }

    @Override
    public Long getProductId() {
        return product != null ? product.getId() : null;
    }

    @Override
    public Long getVariantId() {
        return variant != null ? variant.getId() : null;
    }

    @Override
    public Long getBatchId() {
        return batch != null ? batch.getId() : null;
    }

    @Override
    public String getBatchNo() {
        if (batchNo != null && !batchNo.trim().isEmpty()) {
            return batchNo.trim();
        }
        try {
            return batch != null ? batch.getBatchNo() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public List<? extends PosLineItemDiscount> getLineDiscounts() {
        return discounts;
    }

    public Long resolveSourceDetailId() {
        if (referenceTransactionDetail != null && referenceTransactionDetail.getId() != null) {
            return referenceTransactionDetail.getId();
        }
        try {
            if (transaction != null
                    && transaction.getReferenceTransaction() != null
                    && transaction.getReferenceTransaction().getDetails() != null
                    && product != null
                    && product.getId() != null) {
                Long prodId = product.getId();
                Long varId = variant != null ? variant.getId() : null;
                Long bId = batch != null ? batch.getId() : null;
                String bNo = getBatchNo();

                for (RetailTransactionDetail refDetail : transaction.getReferenceTransaction().getDetails()) {
                    if (refDetail.getId() == null || refDetail.getProduct() == null || !prodId.equals(refDetail.getProduct().getId())) {
                        continue;
                    }
                    Long refVarId = refDetail.getVariant() != null ? refDetail.getVariant().getId() : null;
                    if (!java.util.Objects.equals(varId, refVarId)) {
                        continue;
                    }
                    Long refBatchId = refDetail.getBatch() != null ? refDetail.getBatch().getId() : null;
                    String refBatchNo = refDetail.getBatchNo();
                    boolean batchMatches = (bId == null && refBatchId == null)
                            || java.util.Objects.equals(bId, refBatchId)
                            || (bNo != null && refBatchNo != null && bNo.equalsIgnoreCase(refBatchNo));
                    if (batchMatches) {
                        return refDetail.getId();
                    }
                }

                for (RetailTransactionDetail refDetail : transaction.getReferenceTransaction().getDetails()) {
                    if (refDetail.getId() == null || refDetail.getProduct() == null || !prodId.equals(refDetail.getProduct().getId())) {
                        continue;
                    }
                    Long refVarId = refDetail.getVariant() != null ? refDetail.getVariant().getId() : null;
                    if (java.util.Objects.equals(varId, refVarId)) {
                        return refDetail.getId();
                    }
                }

                for (RetailTransactionDetail refDetail : transaction.getReferenceTransaction().getDetails()) {
                    if (refDetail.getId() != null && refDetail.getProduct() != null && prodId.equals(refDetail.getProduct().getId())) {
                        return refDetail.getId();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
