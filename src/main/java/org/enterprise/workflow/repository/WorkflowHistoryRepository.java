package org.enterprise.workflow.repository;

import org.enterprise.workflow.entity.WorkflowHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowHistoryRepository
        extends JpaRepository<WorkflowHistory, Long> {

    List<WorkflowHistory> findByInstanceIdOrderByActionTimeAsc(
            Long instanceId
    );
}