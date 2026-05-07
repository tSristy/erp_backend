package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.StockBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockBalanceRepository
        extends JpaRepository<StockBalance, Long> {

    Optional<StockBalance> findByProductIdAndWarehouseIdAndLocationId(
            Long itemId,
            Long warehouseId,
            Long locationId
    );
}