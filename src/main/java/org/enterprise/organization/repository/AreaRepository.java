package org.enterprise.organization.repository;

import org.enterprise.organization.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long>, JpaSpecificationExecutor<Area> {
    Optional<Area> findTopByCodeStartingWithOrderByIdDesc(String prefix);
}
