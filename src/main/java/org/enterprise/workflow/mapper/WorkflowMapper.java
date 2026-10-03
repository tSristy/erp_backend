package org.enterprise.workflow.mapper;

import org.enterprise.workflow.dto.WorkflowDefinitionDto;
import org.enterprise.workflow.entity.WorkflowDefinition;
import java.util.List;

public interface WorkflowMapper {
    WorkflowDefinition toEntity(WorkflowDefinitionDto dto, WorkflowDefinition existingEntity);
    WorkflowDefinitionDto toDto(WorkflowDefinition entity);
    List<WorkflowDefinitionDto> toDtoList(List<WorkflowDefinition> entityList);
}
