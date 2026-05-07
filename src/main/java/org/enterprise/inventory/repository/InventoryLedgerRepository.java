package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.InventoryLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryLedgerRepository
        extends JpaRepository<InventoryLedger, Long> {

    List<InventoryLedger> findByProductIdAndWarehouseId(
            Long itemId,
            Long warehouseId
    );
}