package org.enterprise.finance.repository;

import org.enterprise.finance.entity.DimensionBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface DimensionBalanceRepository extends JpaRepository<DimensionBalance, Long> {

    @Query("SELECT d FROM DimensionBalance d WHERE d.fiscalPeriod.id = :periodId AND d.dimensionType = :dimType AND d.dimensionCode = :dimCode AND d.account.id = :accountId")
    Optional<DimensionBalance> findBalance(@Param("periodId") Long periodId, @Param("dimType") String dimType, @Param("dimCode") String dimCode, @Param("accountId") Long accountId);

    @Query("SELECT d FROM DimensionBalance d WHERE d.fiscalPeriod.id = :periodId AND d.dimensionType = :dimType AND d.dimensionCode = :dimCode")
    List<DimensionBalance> findByPeriodAndDimension(@Param("periodId") Long periodId, @Param("dimType") String dimType, @Param("dimCode") String dimCode);

    @Query("SELECT SUM(d.openingBalance) FROM DimensionBalance d WHERE d.fiscalPeriod.id = :periodId AND d.dimensionType = :dimType AND d.dimensionCode = :dimCode")
    BigDecimal calculateTotalDimensionBalance(@Param("periodId") Long periodId, @Param("dimType") String dimType, @Param("dimCode") String dimCode);
}
