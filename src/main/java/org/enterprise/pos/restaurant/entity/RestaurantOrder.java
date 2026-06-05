package org.enterprise.pos.restaurant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.Warehouse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pos_restaurant_orders")
@Getter
@Setter
public class RestaurantOrder extends AuditableEntity {

    private String orderNo;

    private LocalDateTime orderDate;

    private String tableNumber;
    
    private Long waiterId; // ID of the waiter who served this table

    @Enumerated(EnumType.STRING)
    private OrderType orderType = OrderType.DINE_IN;
    
    private LocalDateTime eventDateTime; // For PRE_ORDER

    @Enumerated(EnumType.STRING)
    private TransactionType type = TransactionType.SALES;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_order_id")
    private RestaurantOrder referenceOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private BusinessPartner customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    private RestaurantOrderStatus status = RestaurantOrderStatus.NEW;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantOrderDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantPayment> payments = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KitchenOrderTicket> kots = new ArrayList<>();

    public enum RestaurantOrderStatus {
        NEW, KOT_SENT, SERVED, BILLED, PAID, CANCELLED
    }

    public enum OrderType {
        DINE_IN, TAKE_AWAY, DELIVERY, PRE_ORDER
    }

    public enum TransactionType {
        SALES, RETURN
    }

    public void addDetail(RestaurantOrderDetail detail) {
        details.add(detail);
        detail.setOrder(this);
    }

    public void addPayment(RestaurantPayment payment) {
        payments.add(payment);
        payment.setOrder(this);
    }
    
    public void addKot(KitchenOrderTicket kot) {
        kots.add(kot);
        kot.setOrder(this);
    }
}
