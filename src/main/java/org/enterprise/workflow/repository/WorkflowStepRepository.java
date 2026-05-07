package org.enterprise.workflow.repository;

import org.enterprise.workflow.entity.WorkflowDefinition;
import org.enterprise.workflow.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowStepRepository
        extends JpaRepository<WorkflowStep, Long> {

    List<WorkflowStep> findByWorkflowOrderByStepNo(
            WorkflowDefinition workflow
    );
}