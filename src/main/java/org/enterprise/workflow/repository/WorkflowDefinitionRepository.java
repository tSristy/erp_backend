package org.enterprise.workflow.repository;

import org.enterprise.workflow.entity.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkflowDefinitionRepository
        extends JpaRepository<WorkflowDefinition, Long> {

    Optional<WorkflowDefinition> findByCode(String code);
}