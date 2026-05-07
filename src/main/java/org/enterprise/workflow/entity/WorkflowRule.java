package org.enterprise.workflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

@Entity
@Table(name = "wf_rules")
@Getter
@Setter
public class WorkflowRule extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private WorkflowDefinition workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    private WorkflowStep step;

    private String fieldName;

    // amount
    // departmentId
    // branchId

    private String operator;

    // > < >= <= == != IN

    private String value1;

    private String value2;

    private Integer priority;
}

/*Value-Based Approval Examples
Purchase Approval
Amount	Approval
<= 5,000	Manager
5,001 - 50,000	Finance
> 50,000	Director
> 500,000	CEO*/
