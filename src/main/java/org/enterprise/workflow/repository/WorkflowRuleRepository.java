package org.enterprise.workflow.repository;

import org.enterprise.workflow.entity.WorkflowDefinition;
import org.enterprise.workflow.entity.WorkflowRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowRuleRepository
        extends JpaRepository<WorkflowRule, Long> {

    List<WorkflowRule> findByWorkflowOrderByPriorityAsc(
            WorkflowDefinition workflow
    );
}