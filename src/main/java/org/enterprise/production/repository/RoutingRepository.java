package org.enterprise.production.repository;

import org.enterprise.production.entity.Routing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutingRepository extends JpaRepository<Routing, Long> {
    List<Routing> findByCompanyId(Long companyId);
}
