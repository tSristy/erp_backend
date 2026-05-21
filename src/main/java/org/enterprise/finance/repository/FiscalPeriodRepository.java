package org.enterprise.finance.repository;

import org.enterprise.finance.entity.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, Long> {
}
