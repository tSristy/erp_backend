package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.StockReclassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockReclassificationRepository extends JpaRepository<StockReclassification, Long> {
}
