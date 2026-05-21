package org.enterprise.finance.repository;

import org.enterprise.finance.entity.FiscalYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FiscalYearRepository extends JpaRepository<FiscalYear, Long> {
}
