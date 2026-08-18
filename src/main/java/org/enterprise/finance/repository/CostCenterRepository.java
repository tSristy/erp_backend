package org.enterprise.finance.repository;

import org.enterprise.finance.entity.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, Long> {
    CostCenter findByCodeAndCompanyId(String code, Long companyId);
    List<CostCenter> findByCompanyId(Long companyId);
    long countByCompanyId(Long companyId);
}
