package org.enterprise.pos.retail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
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
    private RetailTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductVariant variant;

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
    public List<? extends PosLineItemDiscount> getLineDiscounts() {
        return discounts;
    }
}
