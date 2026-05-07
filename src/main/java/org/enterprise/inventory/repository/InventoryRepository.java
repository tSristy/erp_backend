package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByProductIdAndCompanyId(Long productId, Long companyId);

    List<Inventory> findByLocationId(Long locationId);
}