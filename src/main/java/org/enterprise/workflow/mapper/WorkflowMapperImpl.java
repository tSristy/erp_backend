package org.enterprise.workflow.mapper;

import lombok.RequiredArgsConstructor;
import org.enterprise.workflow.dto.WorkflowDefinitionDto;
import org.enterprise.workflow.dto.WorkflowRuleDto;
import org.enterprise.workflow.dto.WorkflowStepDto;
import org.enterprise.workflow.entity.WorkflowDefinition;
import org.enterprise.workflow.entity.WorkflowRule;
import org.enterprise.workflow.entity.WorkflowStep;
import org.enterprise.security.repository.RoleRepository;
import org.enterprise.security.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WorkflowMapperImpl implements WorkflowMapper {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public WorkflowDefinition toEntity(WorkflowDefinitionDto dto, WorkflowDefinition entity) {
        if (dto == null) {
            return null;
        }
        if (entity == null) {
            entity = new WorkflowDefinition();
        }

        BeanUtils.copyProperties(dto, entity, "id", "steps", "rules", "createdAt", "updatedAt", "createdBy", "updatedBy", "deleted", "deletedAt", "deletedBy", "deleteReason");

        if (dto.getSteps() != null) {
            if (entity.getSteps() == null) entity.setSteps(new ArrayList<>());
            entity.getSteps().removeIf(existing -> dto.getSteps().stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for (WorkflowStepDto stepDto : dto.getSteps()) {
                WorkflowStep step;
                if (stepDto.getId() == null || entity.getSteps().stream().noneMatch(s -> s.getId() != null && s.getId().equals(stepDto.getId()))) {
                    step = new WorkflowStep();
                    entity.getSteps().add(step);
                } else {
                    step = entity.getSteps().stream().filter(s -> s.getId() != null && s.getId().equals(stepDto.getId())).findFirst().orElse(new WorkflowStep());
                }
                BeanUtils.copyProperties(stepDto, step, "id", "createdAt", "updatedAt", "createdBy", "updatedBy", "deleted", "deletedAt", "deletedBy", "deleteReason");
                step.setWorkflow(entity);
                if (stepDto.getRoleId() != null) {
                    step.setRole(roleRepository.findById(stepDto.getRoleId()).orElse(null));
                }
                if (stepDto.getUserId() != null) {
                    step.setUser(userRepository.findById(stepDto.getUserId()).orElse(null));
                }
            }
        } else if (entity.getSteps() != null) {
            entity.getSteps().clear();
        }

        if (dto.getRules() != null) {
            if (entity.getRules() == null) entity.setRules(new ArrayList<>());
            entity.getRules().removeIf(existing -> dto.getRules().stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for (WorkflowRuleDto ruleDto : dto.getRules()) {
                WorkflowRule rule;
                if (ruleDto.getId() == null || entity.getRules().stream().noneMatch(r -> r.getId() != null && r.getId().equals(ruleDto.getId()))) {
                    rule = new WorkflowRule();
                    entity.getRules().add(rule);
                } else {
                    rule = entity.getRules().stream().filter(r -> r.getId() != null && r.getId().equals(ruleDto.getId())).findFirst().orElse(new WorkflowRule());
                }
                BeanUtils.copyProperties(ruleDto, rule, "id", "createdAt", "updatedAt", "createdBy", "updatedBy", "deleted", "deletedAt", "deletedBy", "deleteReason");
                rule.setWorkflow(entity);
                if (ruleDto.getStepId() != null) {
                    rule.setStep(entity.getSteps().stream()
                            .filter(s -> s.getStepNo() != null && s.getStepNo().equals(ruleDto.getStepId().intValue()))
                            .findFirst()
                            .orElse(null));
                }
            }
        } else if (entity.getRules() != null) {
            entity.getRules().clear();
        }

        return entity;
    }

    @Override
    public WorkflowDefinitionDto toDto(WorkflowDefinition entity) {
        if (entity == null) {
            return null;
        }
        WorkflowDefinitionDto dto = new WorkflowDefinitionDto();
        BeanUtils.copyProperties(entity, dto, "steps", "rules");
        
        if (entity.getSteps() != null) {
            dto.setSteps(entity.getSteps().stream().map(step -> {
                WorkflowStepDto stepDto = new WorkflowStepDto();
                BeanUtils.copyProperties(step, stepDto);
                if (step.getRole() != null) stepDto.setRoleId(step.getRole().getId());
                if (step.getUser() != null) stepDto.setUserId(step.getUser().getId());
                return stepDto;
            }).collect(Collectors.toList()));
        }
        
        if (entity.getRules() != null) {
            dto.setRules(entity.getRules().stream().map(rule -> {
                WorkflowRuleDto ruleDto = new WorkflowRuleDto();
                BeanUtils.copyProperties(rule, ruleDto);
                if (rule.getStep() != null) ruleDto.setStepId(Long.valueOf(rule.getStep().getStepNo()));
                return ruleDto;
            }).collect(Collectors.toList()));
        }
        
        return dto;
    }

    @Override
    public List<WorkflowDefinitionDto> toDtoList(List<WorkflowDefinition> entityList) {
        if (entityList == null) {
            return null;
        }
        return entityList.stream().map(this::toDto).collect(Collectors.toList());
    }
}
