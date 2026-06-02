package org.enterprise.pos.restaurant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pos_kitchen_order_tickets")
@Getter
@Setter
public class KitchenOrderTicket extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private RestaurantOrder order;

    private String kotNumber;

    private LocalDateTime sentTime;

    @Enumerated(EnumType.STRING)
    private KotStatus status = KotStatus.PENDING;

    @OneToMany(mappedBy = "kot", cascade = CascadeType.ALL)
    private List<RestaurantOrderDetail> details = new ArrayList<>();

    public enum KotStatus {
        PENDING, PREPARING, READY, SERVED, CANCELLED
    }
}
