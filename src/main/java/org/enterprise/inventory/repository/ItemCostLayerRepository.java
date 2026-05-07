package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.ItemCostLayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemCostLayerRepository
        extends JpaRepository<ItemCostLayer, Long> {

    List<ItemCostLayer> findByProductIdAndWarehouseIdAndClosedFalseOrderByCreatedAtAsc(
            Long itemId,
            Long warehouseId
    );
}