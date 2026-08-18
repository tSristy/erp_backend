package org.enterprise.workflow.dto;

import lombok.Data;

@Data
public class WorkflowRuleDto {
    private Long id;
    private Long stepId;
    private String fieldName;
    private String operator;
    private String value1;
    private String value2;
    private Integer priority;
}
