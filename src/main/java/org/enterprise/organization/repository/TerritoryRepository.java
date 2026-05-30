package org.enterprise.organization.repository;

import org.enterprise.organization.entity.Territory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerritoryRepository extends JpaRepository<Territory, Long> {
}
