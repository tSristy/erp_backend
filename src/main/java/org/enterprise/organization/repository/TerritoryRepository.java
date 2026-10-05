package org.enterprise.organization.repository;

import org.enterprise.organization.entity.Territory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TerritoryRepository extends JpaRepository<Territory, Long>, JpaSpecificationExecutor<Territory> {
    Optional<Territory> findTopByCodeStartingWithOrderByIdDesc(String prefix);
}
