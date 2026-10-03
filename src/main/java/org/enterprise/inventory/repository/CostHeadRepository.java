package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.CostHead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface CostHeadRepository extends JpaRepository<CostHead, Long>, JpaSpecificationExecutor<CostHead> {
    List<CostHead> findByCompanyId(Long companyId);
}
