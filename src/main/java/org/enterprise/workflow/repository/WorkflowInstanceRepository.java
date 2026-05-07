package org.enterprise.workflow.repository;

import org.enterprise.workflow.entity.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowInstanceRepository
        extends JpaRepository<WorkflowInstance, Long> {

    List<WorkflowInstance> findByStatus(String status);

    List<WorkflowInstance> findByInitiatedBy(Long userId);
}