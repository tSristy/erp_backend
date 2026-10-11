package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.StockReclassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockReclassificationRepository extends JpaRepository<StockReclassification, Long>, JpaSpecificationExecutor<StockReclassification> {
    Optional<StockReclassification> findTopByReclassNoStartingWithOrderByIdDesc(String prefix);
}
