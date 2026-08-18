package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.InventoryCostLayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryCostLayerRepository extends JpaRepository<InventoryCostLayer, Long> {

    @Query("SELECT l FROM InventoryCostLayer l WHERE l.product.id = :productId AND l.warehouse.id = :warehouseId AND l.remainingQty > 0 ORDER BY l.receiptDate ASC")
    List<InventoryCostLayer> findFifoLayers(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    @Query("SELECT l FROM InventoryCostLayer l WHERE l.product.id = :productId AND l.warehouse.id = :warehouseId AND l.remainingQty > 0 ORDER BY l.receiptDate DESC")
    List<InventoryCostLayer> findLifoLayers(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);
    
    List<InventoryCostLayer> findByDocumentTypeAndDocumentId(String documentType, Long documentId);
}
