package org.enterprise.common.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@SQLRestriction("deleted = false")
public abstract class AuditableEntity extends TenantEntity {

    private Long createdBy;
    private Long updatedBy;

    private Boolean deleted = false;

    private LocalDateTime deletedAt;
    private Long deletedBy;
    private String deleteReason;
}
