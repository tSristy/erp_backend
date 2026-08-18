package org.enterprise.workflow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.security.entity.Role;
import org.enterprise.security.entity.User;

@Entity
@Table(name = "wf_steps")
@Getter
@Setter
public class WorkflowStep extends AuditableEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private WorkflowDefinition workflow;

    private Integer stepNo;

    private String name;

    private String approvalType;

    // USER / ROLE / DESIGNATION / DYNAMIC

    @ManyToOne(fetch = FetchType.LAZY)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private Boolean mandatory = true;

    private Boolean parallelApproval = false;

    private Integer escalationHours;

    private Boolean finalStep = false;
}
