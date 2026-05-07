package org.enterprise.workflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "wf_instances")
@Getter
@Setter
public class WorkflowInstance extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private WorkflowDefinition workflow;

    private String entityName;

    private Long entityId;

    private String documentNo;

    private String status;

    // PENDING
    // APPROVED
    // REJECTED
    // CANCELLED

    private Integer currentStepNo;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private Long initiatedBy;
}

