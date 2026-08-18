package org.enterprise.finance.repository;

import org.enterprise.finance.entity.ProfitCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfitCenterRepository extends JpaRepository<ProfitCenter, Long> {
    long countByCompanyId(Long companyId);
}
