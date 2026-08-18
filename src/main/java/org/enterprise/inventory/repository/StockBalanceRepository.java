package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.StockBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockBalanceRepository
        extends JpaRepository<StockBalance, Long> {

    Optional<StockBalance> findByProductIdAndWarehouseIdAndLocationIdAndBatchId(
            Long itemId,
            Long warehouseId,
            Long locationId,
            Long batchId
    );
    
    Optional<StockBalance> findByProductIdAndWarehouseIdAndLocationIdAndBatchIsNull(
            Long itemId,
            Long warehouseId,
            Long locationId
    );

    @org.springframework.data.jpa.repository.Query("SELECT sb FROM StockBalance sb WHERE sb.product.id = :productId AND sb.warehouse.id = :warehouseId AND sb.quantity > 0 AND sb.batch IS NOT NULL ORDER BY sb.batch.expiryDate ASC, sb.batch.id ASC")
    java.util.List<StockBalance> findAvailableBatchesForIssue(
            @org.springframework.data.repository.query.Param("productId") Long productId, 
            @org.springframework.data.repository.query.Param("warehouseId") Long warehouseId
    );
}