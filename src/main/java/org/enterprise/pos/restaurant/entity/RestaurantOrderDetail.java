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
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class RestaurantOrderDetail extends AuditableEntity implements PosLineItem {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private RestaurantOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kot_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
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

    @Transient
    private Long transientProductId;

    @Override
    @com.fasterxml.jackson.annotation.JsonProperty("productId")
    public Long getProductId() {
        return product != null ? product.getId() : transientProductId;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("productId")
    public void setProductId(Long productId) {
        this.transientProductId = productId;
        if (productId != null) {
            if (this.product == null) {
                this.product = new Product();
            }
            this.product.setId(productId);
        }
    }

    @com.fasterxml.jackson.annotation.JsonProperty("product")
    public void setProductPayload(Object prod) {
        if (prod instanceof Number) {
            setProductId(((Number) prod).longValue());
        } else if (prod instanceof String) {
            try {
                setProductId(Long.parseLong((String) prod));
            } catch (Exception ignored) {}
        } else if (prod instanceof java.util.Map) {
            Object idVal = ((java.util.Map<?, ?>) prod).get("id");
            if (idVal instanceof Number) {
                setProductId(((Number) idVal).longValue());
            } else if (idVal instanceof String) {
                try {
                    setProductId(Long.parseLong((String) idVal));
                } catch (Exception ignored) {}
            }
        } else if (prod instanceof Product) {
            this.product = (Product) prod;
        }
    }

    @com.fasterxml.jackson.annotation.JsonProperty("productName")
    public String getProductName() {
        try {
            return product != null ? product.getName() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public List<? extends PosLineItemDiscount> getLineDiscounts() {
        return discounts;
    }
}
