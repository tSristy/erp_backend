package org.enterprise.workflow.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.security.entity.User;
import org.enterprise.security.repository.UserRepository;
import org.enterprise.workflow.dto.WorkflowActionRequest;
import org.enterprise.workflow.dto.WorkflowStartRequest;
import org.enterprise.workflow.dto.WorkflowTaskDto;
import org.enterprise.workflow.entity.*;
import org.enterprise.workflow.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.enterprise.workflow.event.WorkflowStatusEvent;

import java.time.LocalDateTime;
import java.util.List;
import org.enterprise.workflow.dto.WorkflowDefinitionDto;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowService {

        private final WorkflowDefinitionRepository definitionRepository;
        private final WorkflowStepRepository stepRepository;
        private final WorkflowRuleRepository ruleRepository;
        private final WorkflowInstanceRepository instanceRepository;
        private final WorkflowTaskRepository taskRepository;
        private final WorkflowHistoryRepository historyRepository;
        private final WorkflowRuleEngine ruleEngine;
        private final ApplicationEventPublisher eventPublisher;
        private final UserRepository userRepository;

        // =========================
        // START WORKFLOW
        // =========================

        public WorkflowInstance startWorkflow(
                        WorkflowStartRequest request) {

                WorkflowDefinition definition = definitionRepository
                                .findByCode(request.getWorkflowCode())
                                .orElseGet(() -> ensureDefaultWorkflowDefinition(request));

                WorkflowInstance instance = new WorkflowInstance();

                instance.setWorkflow(definition);
                instance.setEntityId(request.getEntityId());
                instance.setEntityName(request.getEntityName());
                instance.setDocumentNo(request.getDocumentNo());
                instance.setStatus("PENDING");
                instance.setCurrentStepNo(1);
                instance.setStartedAt(LocalDateTime.now());
                instance.setInitiatedBy(TenantContext.get().getUserId());
                instance.setCompanyId(TenantContext.get().getCompanyId());

                instance = instanceRepository.save(instance);

                List<WorkflowStep> steps = resolveSteps(
                                definition,
                                request.getAmount());

                if (steps.isEmpty()) {
                        steps = ensureDefaultWorkflowSteps(definition);
                }

                createTasks(instance, steps);

                return instance;
        }

        private WorkflowDefinition ensureDefaultWorkflowDefinition(WorkflowStartRequest request) {
                String code = request.getWorkflowCode() != null ? request.getWorkflowCode().trim() : "PO_APPROVAL";
                WorkflowDefinition def = new WorkflowDefinition();
                def.setCode(code);
                if ("PO_APPROVAL".equalsIgnoreCase(code)) {
                        def.setName("Purchase Order Approval");
                        def.setModule("INVENTORY");
                        def.setEntityName(request.getEntityName() != null ? request.getEntityName() : "PurchaseOrder");
                } else {
                        def.setName(code.replace('_', ' '));
                        def.setModule("GENERAL");
                        def.setEntityName(request.getEntityName() != null ? request.getEntityName() : "Document");
                }
                def.setActive(true);
                def.setCompanyId(TenantContext.getCompanyId());
                return definitionRepository.save(def);
        }

        private List<WorkflowStep> ensureDefaultWorkflowSteps(WorkflowDefinition definition) {
                List<WorkflowStep> existingSteps = stepRepository.findByWorkflowOrderByStepNo(definition);
                if (!existingSteps.isEmpty()) {
                        return existingSteps;
                }

                Long currentUserId = TenantContext.get() != null ? TenantContext.get().getUserId() : null;
                User assignedUser = null;
                if (currentUserId != null) {
                        assignedUser = userRepository.findById(currentUserId).orElse(null);
                }
                if (assignedUser == null) {
                        assignedUser = userRepository.findAll().stream()
                                        .filter(u -> Boolean.TRUE.equals(u.getActive()))
                                        .findFirst()
                                        .orElse(null);
                }

                WorkflowStep defaultStep = new WorkflowStep();
                defaultStep.setStepNo(1);
                defaultStep.setName("Manager Approval");
                defaultStep.setApprovalType("USER");
                defaultStep.setMandatory(true);
                defaultStep.setFinalStep(true);
                defaultStep.setWorkflow(definition);
                defaultStep.setUser(assignedUser);
                defaultStep.setCompanyId(TenantContext.getCompanyId());

                return List.of(stepRepository.save(defaultStep));
        }

        // =========================
        // RESOLVE STEPS
        // =========================

        private List<WorkflowStep> resolveSteps(
                        WorkflowDefinition definition,
                        java.math.BigDecimal amount) {

                List<WorkflowStep> allSteps = stepRepository.findByWorkflowOrderByStepNo(definition);

                List<WorkflowRule> rules = ruleRepository.findByWorkflowOrderByPriorityAsc(definition);

                return allSteps.stream()
                                .filter(step -> {

                                        List<WorkflowRule> stepRules = rules.stream()
                                                        .filter(r -> r.getStep().getId().equals(step.getId()))
                                                        .toList();

                                        if (stepRules.isEmpty()) {
                                                return true;
                                        }

                                        return stepRules.stream().allMatch(rule -> ruleEngine.evaluate(
                                                        amount,
                                                        rule.getOperator(),
                                                        rule.getValue1()));
                                })
                                .toList();
        }

        // =========================
        // CREATE TASKS
        // =========================

        private void createTasks(
                        WorkflowInstance instance,
                        List<WorkflowStep> steps) {

                if (steps.isEmpty()) {
                        throw new RuntimeException("No workflow steps resolved");
                }

                WorkflowStep firstStep = steps.get(0);

                WorkflowTask task = new WorkflowTask();

                task.setInstance(instance);
                task.setStep(firstStep);
                task.setAssignedUser(resolveStepAssignee(firstStep));
                task.setStatus("PENDING");
                task.setCompanyId(instance.getCompanyId());

                taskRepository.save(task);
        }

        private User resolveStepAssignee(WorkflowStep step) {
                if (step.getUser() != null) {
                        return step.getUser();
                }
                if (step.getRole() != null && step.getRole().getId() != null) {
                        Long targetRoleId = step.getRole().getId();
                        User roleUser = userRepository.findByActiveTrue().stream()
                                        .filter(u -> u.getRoles() != null && u.getRoles().stream()
                                                        .anyMatch(ur -> Boolean.TRUE.equals(ur.getActive())
                                                                        && ur.getRole() != null
                                                                        && targetRoleId.equals(ur.getRole().getId())))
                                        .findFirst()
                                        .orElse(null);
                        if (roleUser != null) {
                                return roleUser;
                        }
                }
                Long currentUserId = TenantContext.get() != null ? TenantContext.get().getUserId() : null;
                if (currentUserId != null) {
                        return userRepository.findById(currentUserId).orElse(null);
                }
                return null;
        }

        // =========================
        // APPROVE TASK
        // =========================

        public void approve(
                        WorkflowActionRequest request) {

                WorkflowTask task = taskRepository
                                .findById(request.getTaskId())
                                .orElseThrow(() -> new RuntimeException("Task not found"));

                if (task.getAssignedUser() != null && !task.getAssignedUser().getId()
                                .equals(TenantContext.get().getUserId())) {

                        throw new RuntimeException("Unauthorized approval");
                }

                task.setStatus("APPROVED");
                task.setRemarks(request.getRemarks());
                task.setActionAt(LocalDateTime.now());

                taskRepository.save(task);

                saveHistory(task, "APPROVED", request.getRemarks());

                moveNext(task);
        }

        // =========================
        // REJECT TASK
        // =========================

        public void reject(
                        WorkflowActionRequest request) {

                WorkflowTask task = taskRepository
                                .findById(request.getTaskId())
                                .orElseThrow(() -> new RuntimeException("Task not found"));

                task.setStatus("REJECTED");
                task.setRemarks(request.getRemarks());
                task.setActionAt(LocalDateTime.now());

                taskRepository.save(task);

                WorkflowInstance instance = task.getInstance();

                instance.setStatus("REJECTED");

                instanceRepository.save(instance);

                saveHistory(task, "REJECTED", request.getRemarks());
                
                eventPublisher.publishEvent(new WorkflowStatusEvent(
                        this,
                        instance.getEntityName(),
                        instance.getEntityId(),
                        "REJECTED"
                ));
        }

        // =========================
        // MOVE NEXT STEP
        // =========================

        private void moveNext(
                        WorkflowTask task) {

                WorkflowInstance instance = task.getInstance();

                int nextStepNo = task.getStep().getStepNo() + 1;

                List<WorkflowStep> steps = stepRepository
                                .findByWorkflowOrderByStepNo(
                                                instance.getWorkflow());

                WorkflowStep nextStep = steps.stream()
                                .filter(s -> s.getStepNo() == nextStepNo)
                                .findFirst()
                                .orElse(null);

                // FINAL APPROVAL
                if (nextStep == null) {

                        instance.setStatus("APPROVED");
                        instance.setCompletedAt(LocalDateTime.now());

                        instanceRepository.save(instance);
                        
                        eventPublisher.publishEvent(new WorkflowStatusEvent(
                                this,
                                instance.getEntityName(),
                                instance.getEntityId(),
                                "APPROVED"
                        ));

                        return;
                }

                instance.setCurrentStepNo(nextStepNo);
                instanceRepository.save(instance);

                WorkflowTask nextTask = new WorkflowTask();

                nextTask.setInstance(instance);
                nextTask.setStep(nextStep);
                nextTask.setAssignedUser(resolveStepAssignee(nextStep));
                nextTask.setStatus("PENDING");
                nextTask.setCompanyId(instance.getCompanyId());

                taskRepository.save(nextTask);
        }

        // =========================
        // HISTORY
        // =========================

        private void saveHistory(
                        WorkflowTask task,
                        String action,
                        String remarks) {

                WorkflowHistory history = new WorkflowHistory();

                history.setInstance(task.getInstance());
                history.setStepNo(task.getStep().getStepNo());
                history.setUserId(TenantContext.get().getUserId());
                history.setAction(action);
                history.setRemarks(remarks);
                history.setActionTime(LocalDateTime.now());
                history.setCompanyId(task.getCompanyId());

                historyRepository.save(history);
        }

        // =========================
        // USER TASKS
        // =========================

        public List<WorkflowTaskDto> myPendingTasks() {

                List<WorkflowTask> tasks = taskRepository.findByAssignedUserIdAndStatus(TenantContext.get().getUserId(), "PENDING");
                
                return tasks.stream().map(task -> {
                    WorkflowTaskDto dto = new WorkflowTaskDto();
                    dto.setId(task.getId());
                    if (task.getInstance() != null) {
                        dto.setInstanceId(task.getInstance().getId());
                        dto.setEntityName(task.getInstance().getEntityName());
                        dto.setDocumentNo(task.getInstance().getDocumentNo());
                        dto.setStartedAt(task.getInstance().getStartedAt());
                    }
                    if (task.getStep() != null) {
                        dto.setStepNo(task.getStep().getStepNo());
                        dto.setStepName(task.getStep().getName());
                    }
                    dto.setStatus(task.getStatus());
                    dto.setRemarks(task.getRemarks());
                    dto.setActionAt(task.getActionAt());
                    return dto;
                }).toList();
        }
}