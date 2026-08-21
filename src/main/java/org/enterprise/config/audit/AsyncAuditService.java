package org.enterprise.config.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.entity.SystemAuditLog;
import org.enterprise.common.repository.SystemAuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncAuditService {

    private final SystemAuditLogRepository auditLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAuditLog(String entityName, String entityId, String action, String changedBy, String[] propertyNames, Object[] oldState, Object[] newState) {
        try {
            SystemAuditLog auditLog = new SystemAuditLog();
            auditLog.setEntityName(entityName);
            auditLog.setEntityId(entityId);
            auditLog.setAction(action);
            auditLog.setChangedBy(changedBy);
            auditLog.setChangedAt(LocalDateTime.now());

            if (oldState != null && propertyNames != null) {
                auditLog.setOldState(toJson(propertyNames, oldState));
            }
            if (newState != null && propertyNames != null) {
                auditLog.setNewState(toJson(propertyNames, newState));
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log for {} {}", entityName, entityId, e);
        }
    }

    private String toJson(String[] propertyNames, Object[] state) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (int i = 0; i < propertyNames.length; i++) {
            if (state[i] != null && !isProxyOrCollection(state[i])) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(propertyNames[i]).append("\":");
                
                Object val = state[i];
                if (val instanceof Number || val instanceof Boolean) {
                    json.append(val);
                } else {
                    String strVal = val.toString().replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
                    json.append("\"").append(strVal).append("\"");
                }
                first = false;
            }
        }
        json.append("}");
        return json.toString();
    }

    private boolean isProxyOrCollection(Object obj) {
        // Simple check to avoid deep serializing Hibernate collections or proxies
        if (obj instanceof java.util.Collection) return true;
        if (obj instanceof java.util.Map) return true;
        String className = obj.getClass().getName();
        return className.contains("HibernateProxy") || className.contains("PersistentCollection");
    }
}
