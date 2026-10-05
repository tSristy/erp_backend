package org.enterprise.organization.repository;

import org.enterprise.organization.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long>, JpaSpecificationExecutor<Zone> {
    Optional<Zone> findTopByCodeStartingWithOrderByIdDesc(String prefix);
}
