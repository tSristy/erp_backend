package org.enterprise.config.audit;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HibernateListenerConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final HibernateAuditListener hibernateAuditListener;

    @PostConstruct
    public void registerListeners() {
        SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

        if (registry != null) {
            registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(hibernateAuditListener);
            registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(hibernateAuditListener);
            registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(hibernateAuditListener);
        }
    }
}
