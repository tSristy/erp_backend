package org.enterprise.pos.restaurant.entity;

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
@Table(name = "pos_restaurant_order_details")
@Getter
@Setter
public class RestaurantOrderDetail extends AuditableEntity implements PosLineItem {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private RestaurantOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kot_id")
    private KitchenOrderTicket kot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal quantity;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @OneToMany(mappedBy = "orderDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantOrderDetailDiscount> discounts = new ArrayList<>();

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal lineTotal;

    @Enumerated(EnumType.STRING)
    private DetailStatus status = DetailStatus.PENDING;

    public enum DetailStatus {
        PENDING, SENT_TO_KITCHEN, PREPARING, SERVED, CANCELLED
    }

    public void addDiscount(RestaurantOrderDetailDiscount discount) {
        discounts.add(discount);
        discount.setOrderDetail(this);
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
