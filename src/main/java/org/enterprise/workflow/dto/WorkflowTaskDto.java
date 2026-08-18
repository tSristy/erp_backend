package org.enterprise.workflow.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkflowTaskDto {
    private Long id;
    private Long instanceId;
    private String entityName;
    private String documentNo;
    private LocalDateTime startedAt;
    
    private Integer stepNo;
    private String stepName;
    
    private String status;
    private String remarks;
    private LocalDateTime actionAt;
}
