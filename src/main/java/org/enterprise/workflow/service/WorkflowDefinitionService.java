package org.enterprise.workflow.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.workflow.dto.WorkflowDefinitionDto;
import org.enterprise.workflow.dto.WorkflowRuleDto;
import org.enterprise.workflow.dto.WorkflowStepDto;
import org.enterprise.workflow.entity.WorkflowDefinition;
import org.enterprise.workflow.entity.WorkflowRule;
import org.enterprise.workflow.entity.WorkflowStep;
import org.enterprise.workflow.repository.WorkflowDefinitionRepository;
import org.enterprise.security.repository.RoleRepository;
import org.enterprise.security.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowDefinitionService {

    private final WorkflowDefinitionRepository repository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public List<WorkflowDefinitionDto> findAll() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public WorkflowDefinitionDto findById(Long id) {
        WorkflowDefinition entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Workflow Definition not found"));
        return mapToDto(entity);
    }

    private WorkflowDefinitionDto mapToDto(WorkflowDefinition entity) {
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

    public WorkflowDefinitionDto save(WorkflowDefinitionDto dto) {
        WorkflowDefinition entity;
        if (dto.getId() != null) {
            entity = repository.findById(dto.getId()).orElse(new WorkflowDefinition());
        } else {
            entity = new WorkflowDefinition();
        }

        BeanUtils.copyProperties(dto, entity, "id", "steps", "rules");

        if (dto.getSteps() != null) {
            if (entity.getSteps() == null) entity.setSteps(new java.util.ArrayList<>());
            entity.getSteps().removeIf(existing -> dto.getSteps().stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for (WorkflowStepDto stepDto : dto.getSteps()) {
                WorkflowStep step;
                if (stepDto.getId() == null || entity.getSteps().stream().noneMatch(s -> s.getId() != null && s.getId().equals(stepDto.getId()))) {
                    step = new WorkflowStep();
                    entity.getSteps().add(step);
                } else {
                    step = entity.getSteps().stream().filter(s -> s.getId() != null && s.getId().equals(stepDto.getId())).findFirst().orElse(new WorkflowStep());
                }
                BeanUtils.copyProperties(stepDto, step, "id");
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
            if (entity.getRules() == null) entity.setRules(new java.util.ArrayList<>());
            entity.getRules().removeIf(existing -> dto.getRules().stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for (WorkflowRuleDto ruleDto : dto.getRules()) {
                WorkflowRule rule;
                if (ruleDto.getId() == null || entity.getRules().stream().noneMatch(r -> r.getId() != null && r.getId().equals(ruleDto.getId()))) {
                    rule = new WorkflowRule();
                    entity.getRules().add(rule);
                } else {
                    rule = entity.getRules().stream().filter(r -> r.getId() != null && r.getId().equals(ruleDto.getId())).findFirst().orElse(new WorkflowRule());
                }
                BeanUtils.copyProperties(ruleDto, rule, "id");
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

        repository.save(entity);
        return mapToDto(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
