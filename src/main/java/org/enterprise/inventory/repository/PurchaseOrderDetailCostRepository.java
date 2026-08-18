package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.PurchaseOrderDetailCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderDetailCostRepository extends JpaRepository<PurchaseOrderDetailCost, Long> {
}
