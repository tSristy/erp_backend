package org.enterprise.config.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.entity.SystemAuditLog;
import org.enterprise.security.entity.LoginAudit;
import org.hibernate.event.spi.*;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@Component
@RequiredArgsConstructor
@Slf4j
public class HibernateAuditListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    private final AsyncAuditService asyncAuditService;

    @Override
    public void onPostInsert(PostInsertEvent event) {
        if (isAuditable(event.getEntity())) {
            asyncAuditService.saveAuditLog(
                    event.getEntity().getClass().getSimpleName(),
                    getId(event),
                    "INSERT",
                    getCurrentUser(),
                    event.getPersister().getPropertyNames(),
                    null,
                    event.getState()
            );
        }
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        if (isAuditable(event.getEntity())) {
            asyncAuditService.saveAuditLog(
                    event.getEntity().getClass().getSimpleName(),
                    getId(event),
                    "UPDATE",
                    getCurrentUser(),
                    event.getPersister().getPropertyNames(),
                    event.getOldState(),
                    event.getState()
            );
        }
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        if (isAuditable(event.getEntity())) {
            asyncAuditService.saveAuditLog(
                    event.getEntity().getClass().getSimpleName(),
                    getId(event),
                    "DELETE",
                    getCurrentUser(),
                    event.getPersister().getPropertyNames(),
                    event.getDeletedState(),
                    null
            );
        }
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }

    private boolean isAuditable(Object entity) {
        // Do not audit our own audit log entity to prevent infinite loops!
        if (entity instanceof SystemAuditLog) {
            return false;
        }
        // Exclude LoginAudit to avoid spamming the system logs with login attempts
        if (entity instanceof LoginAudit) {
            return false;
        }
        return true;
    }

    private String getId(AbstractEvent event) {
        if (event instanceof PostInsertEvent) return String.valueOf(((PostInsertEvent) event).getId());
        if (event instanceof PostUpdateEvent) return String.valueOf(((PostUpdateEvent) event).getId());
        if (event instanceof PostDeleteEvent) return String.valueOf(((PostDeleteEvent) event).getId());
        return "UNKNOWN";
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return auth.getName();
        }
        return "SYSTEM";
    }
}
