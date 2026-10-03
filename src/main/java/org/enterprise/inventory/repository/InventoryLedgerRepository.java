package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.InventoryLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface InventoryLedgerRepository
        extends JpaRepository<InventoryLedger, Long>, JpaSpecificationExecutor<InventoryLedger> {

    List<InventoryLedger> findByProductIdAndWarehouseId(
            Long itemId,
            Long warehouseId
    );
}