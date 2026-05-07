package org.enterprise.workflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.security.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "wf_tasks")
@Getter
@Setter
public class WorkflowTask extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private WorkflowInstance instance;

    @ManyToOne(fetch = FetchType.LAZY)
    private WorkflowStep step;

    @ManyToOne(fetch = FetchType.LAZY)
    private User assignedUser;

    private String status;

    // PENDING
    // APPROVED
    // REJECTED
    // SENT_BACK

    private String remarks;

    private LocalDateTime actionAt;

    private Boolean delegated = false;
}
