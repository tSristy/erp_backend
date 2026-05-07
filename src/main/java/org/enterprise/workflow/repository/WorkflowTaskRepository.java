package org.enterprise.workflow.repository;

import org.enterprise.workflow.entity.WorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowTaskRepository
        extends JpaRepository<WorkflowTask, Long> {

    List<WorkflowTask> findByAssignedUserIdAndStatus(
            Long userId,
            String status
    );
}