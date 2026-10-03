package org.enterprise.workflow.repository;

import org.enterprise.workflow.entity.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface WorkflowDefinitionRepository
        extends JpaRepository<WorkflowDefinition, Long>, JpaSpecificationExecutor<WorkflowDefinition> {

    Optional<WorkflowDefinition> findByCode(String code);
    Optional<WorkflowDefinition> findByCodeAndCompanyId(String code, Long companyId);
}