package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {
    List<Location> findByWarehouseId(Long warehouseId);
    List<Location> findByParentId(Long parentId);
    List<Location> findByWarehouseIdAndParentIsNull(Long warehouseId);
}