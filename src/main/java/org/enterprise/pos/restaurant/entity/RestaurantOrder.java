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
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
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
    @com.fasterxml.jackson.annotation.JsonIgnore
    private RestaurantOrder referenceOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private BusinessPartner customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
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

    @Transient
    private Long transientWarehouseId;

    @com.fasterxml.jackson.annotation.JsonProperty("warehouseId")
    public Long getWarehouseId() {
        return warehouse != null ? warehouse.getId() : transientWarehouseId;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("warehouseId")
    public void setWarehouseId(Long warehouseId) {
        this.transientWarehouseId = warehouseId;
        if (warehouseId != null) {
            if (this.warehouse == null) {
                this.warehouse = new Warehouse();
            }
            this.warehouse.setId(warehouseId);
        }
    }

    @com.fasterxml.jackson.annotation.JsonProperty("warehouse")
    public void setWarehousePayload(Object wh) {
        if (wh instanceof Number) {
            setWarehouseId(((Number) wh).longValue());
        } else if (wh instanceof String) {
            try {
                setWarehouseId(Long.parseLong((String) wh));
            } catch (Exception ignored) {}
        } else if (wh instanceof java.util.Map) {
            Object idVal = ((java.util.Map<?, ?>) wh).get("id");
            if (idVal instanceof Number) {
                setWarehouseId(((Number) idVal).longValue());
            } else if (idVal instanceof String) {
                try {
                    setWarehouseId(Long.parseLong((String) idVal));
                } catch (Exception ignored) {}
            }
        }
    }

    @com.fasterxml.jackson.annotation.JsonProperty("warehouseName")
    public String getWarehouseName() {
        try {
            return warehouse != null ? warehouse.getName() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Transient
    private Long transientCustomerId;

    @com.fasterxml.jackson.annotation.JsonProperty("customerId")
    public Long getCustomerId() {
        return customer != null ? customer.getId() : transientCustomerId;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("customerId")
    public void setCustomerId(Long customerId) {
        this.transientCustomerId = customerId;
        if (customerId != null) {
            if (this.customer == null) {
                this.customer = new BusinessPartner();
            }
            this.customer.setId(customerId);
        }
    }

    @com.fasterxml.jackson.annotation.JsonProperty("customer")
    public void setCustomerPayload(Object cust) {
        if (cust instanceof Number) {
            setCustomerId(((Number) cust).longValue());
        } else if (cust instanceof String) {
            try {
                setCustomerId(Long.parseLong((String) cust));
            } catch (Exception ignored) {}
        } else if (cust instanceof java.util.Map) {
            Object idVal = ((java.util.Map<?, ?>) cust).get("id");
            if (idVal instanceof Number) {
                setCustomerId(((Number) idVal).longValue());
            } else if (idVal instanceof String) {
                try {
                    setCustomerId(Long.parseLong((String) idVal));
                } catch (Exception ignored) {}
            }
        }
    }

    @com.fasterxml.jackson.annotation.JsonProperty("customerName")
    public String getCustomerName() {
        try {
            return customer != null ? customer.getName() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @com.fasterxml.jackson.annotation.JsonSetter("orderDate")
    public void setOrderDateFromObject(Object dateObj) {
        if (dateObj == null) {
            this.orderDate = LocalDateTime.now();
            return;
        }
        if (dateObj instanceof LocalDateTime) {
            this.orderDate = (LocalDateTime) dateObj;
        } else if (dateObj instanceof String) {
            String str = ((String) dateObj).trim();
            if (str.isEmpty()) {
                this.orderDate = LocalDateTime.now();
                return;
            }
            try {
                if (str.endsWith("Z")) {
                    this.orderDate = java.time.Instant.parse(str)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime();
                } else if (str.contains("T")) {
                    this.orderDate = LocalDateTime.parse(str.substring(0, Math.min(str.length(), 19)));
                } else {
                    this.orderDate = java.time.LocalDate.parse(str).atStartOfDay();
                }
            } catch (Exception e) {
                this.orderDate = LocalDateTime.now();
            }
        }
    }

    @com.fasterxml.jackson.annotation.JsonSetter("eventDateTime")
    public void setEventDateTimeFromObject(Object dateObj) {
        if (dateObj instanceof LocalDateTime) {
            this.eventDateTime = (LocalDateTime) dateObj;
        } else if (dateObj instanceof String) {
            String str = ((String) dateObj).trim();
            if (!str.isEmpty()) {
                try {
                    if (str.endsWith("Z")) {
                        this.eventDateTime = java.time.Instant.parse(str)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime();
                    } else if (str.contains("T")) {
                        this.eventDateTime = LocalDateTime.parse(str.substring(0, Math.min(str.length(), 19)));
                    } else {
                        this.eventDateTime = java.time.LocalDate.parse(str).atStartOfDay();
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    @com.fasterxml.jackson.annotation.JsonProperty("waiterId")
    public void setWaiterIdFromObject(Object wId) {
        if (wId instanceof Number) {
            this.waiterId = ((Number) wId).longValue();
        } else if (wId instanceof String) {
            try {
                this.waiterId = Long.parseLong((String) wId);
            } catch (Exception ignored) {
                this.waiterId = null;
            }
        }
    }
}
