package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long>, JpaSpecificationExecutor<Warehouse> {
    Optional<Warehouse> findTopByCodeStartingWithOrderByIdDesc(String prefix);
}