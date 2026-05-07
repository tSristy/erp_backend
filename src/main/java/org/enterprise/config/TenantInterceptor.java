package org.enterprise.config;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

//“For every request, make sure the database only returns data belonging to the current company.”

@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Long companyId = TenantContext.getCompanyId();

        if (companyId != null) {
            Session session = entityManager.unwrap(Session.class);

            session.enableFilter("tenantFilter")
                    .setParameter("companyId", companyId);
        }

        return true;
    }
}