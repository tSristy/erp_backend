package org.enterprise.workflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "wf_definitions")
@Getter
@Setter
public class WorkflowDefinition extends AuditableEntity {

    @Column(nullable = false)
    private String code;

    private String name;

    private String module;

    private String entityName;

    private Boolean active = true;

    private Integer version = 1;

    private Boolean parallelAllowed = false;

    private Boolean autoApprove = false;

    private Boolean allowReject = true;

    private Boolean allowSendBack = true;
}


/*
Approval Engine Flow
Document Created
    ↓
Find WorkflowDefinition
    ↓
Evaluate Workflow Rules
    ↓
Generate Steps Dynamically
    ↓
Create WorkflowInstance
    ↓
Create WorkflowTask
    ↓
Assign User(s)
    ↓
Approve/Reject
    ↓
Move to Next Step
    ↓
Final Approval

 */