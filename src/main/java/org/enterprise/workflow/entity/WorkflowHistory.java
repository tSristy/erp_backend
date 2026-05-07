package org.enterprise.workflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "wf_history")
@Getter
@Setter
public class WorkflowHistory extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private WorkflowInstance instance;

    private Integer stepNo;

    private Long userId;

    private String action;

    // APPROVED
    // REJECTED
    // FORWARDED
    // SENT_BACK

    private String remarks;

    private LocalDateTime actionTime;
}
