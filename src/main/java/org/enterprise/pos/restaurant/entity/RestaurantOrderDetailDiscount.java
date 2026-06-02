package org.enterprise.pos.restaurant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "pos_restaurant_order_detail_discounts")
@Getter
@Setter
public class RestaurantOrderDetailDiscount extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    private RestaurantOrderDetail orderDetail;

    private String discountName; 

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal discountAmount;
}
