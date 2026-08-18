package org.enterprise.workflow.dto;

import lombok.Data;

@Data
public class WorkflowStepDto {
    private Long id;
    private Integer stepNo;
    private String name;
    private String approvalType;
    private Long roleId;
    private Long userId;
    private Boolean mandatory;
    private Boolean parallelApproval;
    private Integer escalationHours;
    private Boolean finalStep;
}
