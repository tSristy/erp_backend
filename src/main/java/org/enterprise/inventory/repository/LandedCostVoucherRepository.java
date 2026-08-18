package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.LandedCostVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LandedCostVoucherRepository extends JpaRepository<LandedCostVoucher, Long> {
    List<LandedCostVoucher> findByCompanyId(Long companyId);
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(v) FROM LandedCostVoucher v WHERE v.companyId = :companyId AND YEAR(v.postingDate) = :year AND MONTH(v.postingDate) = :month")
    long countByCompanyIdAndYearAndMonth(@org.springframework.data.repository.query.Param("companyId") Long companyId, @org.springframework.data.repository.query.Param("year") int year, @org.springframework.data.repository.query.Param("month") int month);
}
