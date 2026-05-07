package org.enterprise.workflow.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WorkflowStartRequest {

    private String workflowCode;

    private Long entityId;

    private String entityName;

    private String documentNo;

    private BigDecimal amount;
}