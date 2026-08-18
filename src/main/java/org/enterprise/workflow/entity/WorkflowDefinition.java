package org.enterprise.workflow.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkflowStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkflowRule> rules = new ArrayList<>();
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