package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long>, JpaSpecificationExecutor<StockTransfer> {
    Optional<StockTransfer> findTopByTransferNoStartingWithOrderByIdDesc(String prefix);
}
