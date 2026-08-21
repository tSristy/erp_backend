package org.enterprise.production.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "prd_routing_operations")
@Getter
@Setter
public class RoutingOperation extends AuditableEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routing_id")
    private Routing routing;

    @Column(nullable = false)
    private Integer sequence;

    @Column(nullable = false)
    private String operationName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_center_id")
    private WorkCenter workCenter;

    @Column(nullable = false)
    private Integer durationMinutes = 0;
}
