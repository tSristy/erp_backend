package org.enterprise.finance.repository;

import org.enterprise.finance.entity.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, Long> {

    @Query("SELECT p FROM FiscalPeriod p WHERE p.startDate <= :date AND p.endDate >= :date AND p.status = 'OPEN'")
    Optional<FiscalPeriod> findActivePeriodByDate(@Param("date") LocalDate date);

    @Query("SELECT p FROM FiscalPeriod p WHERE p.startDate <= :date AND p.endDate >= :date")
    Optional<FiscalPeriod> findPeriodByDate(@Param("date") LocalDate date);
}
