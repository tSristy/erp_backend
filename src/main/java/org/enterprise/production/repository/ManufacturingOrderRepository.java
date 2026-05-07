package org.enterprise.production.repository;

import org.enterprise.production.entity.ManufacturingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManufacturingOrderRepository extends JpaRepository<ManufacturingOrder, Long> {
}
