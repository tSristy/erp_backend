package org.enterprise.finance.repository;

import org.enterprise.finance.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT l FROM Loan l WHERE l.companyId = :companyId AND " +
           "(LOWER(l.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Loan> searchByCompanyId(@Param("companyId") Long companyId, @Param("search") String search, Pageable pageable);

    Optional<Loan> findByIdAndCompanyId(Long id, Long companyId);

    Optional<Loan> findByCodeAndCompanyId(String code, Long companyId);
}
