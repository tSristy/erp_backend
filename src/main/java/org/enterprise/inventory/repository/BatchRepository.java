package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long>, JpaSpecificationExecutor<Batch> {
    java.util.Optional<Batch> findByBatchNoAndProductId(String batchNo, Long productId);
    java.util.List<Batch> findByCompanyId(Long companyId);
}
