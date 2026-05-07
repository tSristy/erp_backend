package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}