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

    @Override
    protected void onPrePersist() {
        super.onPrePersist();
        if (createdBy == null) {
            createdBy = org.enterprise.common.util.TenantContext.getUserId();
        }
        if (updatedBy == null) {
            updatedBy = org.enterprise.common.util.TenantContext.getUserId();
        }
    }

    @Override
    protected void onPreUpdate() {
        super.onPreUpdate();
        Long currentUserId = org.enterprise.common.util.TenantContext.getUserId();
        if (currentUserId != null) {
            updatedBy = currentUserId;
        }
    }
}
