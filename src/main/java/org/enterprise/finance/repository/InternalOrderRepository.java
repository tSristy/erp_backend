package org.enterprise.finance.repository;

import org.enterprise.finance.entity.InternalOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InternalOrderRepository extends JpaRepository<InternalOrder, Long> {

    @Query("SELECT i FROM InternalOrder i WHERE i.companyId = :companyId AND " +
           "(LOWER(i.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<InternalOrder> searchByCompanyId(@Param("companyId") Long companyId, @Param("search") String search, Pageable pageable);

    Optional<InternalOrder> findByIdAndCompanyId(Long id, Long companyId);

    Optional<InternalOrder> findByCodeAndCompanyId(String code, Long companyId);
}
