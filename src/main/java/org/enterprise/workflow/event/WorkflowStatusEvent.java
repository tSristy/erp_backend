package org.enterprise.workflow.event;

import org.springframework.context.ApplicationEvent;

public class WorkflowStatusEvent extends ApplicationEvent {

    private final String entityName;
    private final Long entityId;
    private final String status;

    public WorkflowStatusEvent(Object source, String entityName, Long entityId, String status) {
        super(source);
        this.entityName = entityName;
        this.entityId = entityId;
        this.status = status;
    }

    public String getEntityName() {
        return entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getStatus() {
        return status;
    }
}
