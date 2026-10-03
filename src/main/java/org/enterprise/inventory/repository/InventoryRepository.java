package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

    List<Inventory> findByProductIdAndCompanyId(Long productId, Long companyId);

    List<Inventory> findByLocationId(Long locationId);
}