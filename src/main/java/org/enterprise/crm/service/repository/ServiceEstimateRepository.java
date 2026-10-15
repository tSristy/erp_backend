package org.enterprise.crm.service.repository;

import org.enterprise.crm.service.entity.ServiceEstimate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceEstimateRepository extends JpaRepository<ServiceEstimate, Long> {
    List<ServiceEstimate> findByServiceRequestId(Long serviceRequestId);

    @Query("SELECT se FROM ServiceEstimate se " +
           "LEFT JOIN se.serviceRequest sr " +
           "WHERE (:companyId IS NULL OR se.companyId = :companyId) AND (" +
           "LOWER(COALESCE(sr.requestNumber, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CAST(se.status AS string)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CAST(se.id AS string)) LIKE LOWER(CONCAT('%', :query, '%'))" +
           ")")
    Page<ServiceEstimate> searchServiceEstimates(
            @Param("companyId") Long companyId,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT se FROM ServiceEstimate se WHERE (:companyId IS NULL OR se.companyId = :companyId)")
    Page<ServiceEstimate> findAllByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
}
