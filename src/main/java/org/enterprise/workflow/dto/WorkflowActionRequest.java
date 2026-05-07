package org.enterprise.workflow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowActionRequest {

    private Long taskId;

    private String remarks;
}