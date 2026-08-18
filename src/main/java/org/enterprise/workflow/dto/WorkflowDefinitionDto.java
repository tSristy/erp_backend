package org.enterprise.workflow.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorkflowDefinitionDto {
    private Long id;
    private String code;
    private String name;
    private String module;
    private String entityName;
    private Boolean active;
    private Integer version;
    private Boolean parallelAllowed;
    private Boolean autoApprove;
    private Boolean allowReject;
    private Boolean allowSendBack;
    
    private List<WorkflowStepDto> steps;
    private List<WorkflowRuleDto> rules;
}
