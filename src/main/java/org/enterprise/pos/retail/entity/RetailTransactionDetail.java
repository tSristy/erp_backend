package org.enterprise.pos.retail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.Product;

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

    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal quantity;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @OneToMany(mappedBy = "transactionDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RetailTransactionDetailDiscount> discounts = new ArrayList<>();

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal lineTotal;

    public void addDiscount(RetailTransactionDetailDiscount discount) {
        discounts.add(discount);
        discount.setTransactionDetail(this);
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
