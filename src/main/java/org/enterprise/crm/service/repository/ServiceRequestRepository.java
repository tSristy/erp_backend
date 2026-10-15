package org.enterprise.crm.service.repository;

import org.enterprise.crm.service.entity.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByRegisteredProductId(Long registeredProductId);
    List<ServiceRequest> findByCustomerId(Long customerId);

    @Query("SELECT sr FROM ServiceRequest sr " +
           "LEFT JOIN sr.customer c " +
           "LEFT JOIN sr.product p " +
           "LEFT JOIN sr.variant v " +
           "LEFT JOIN sr.registeredProduct rp " +
           "WHERE (:companyId IS NULL OR sr.companyId = :companyId) AND (" +
           "LOWER(sr.requestNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sr.issueDescription) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sr.resolutionNotes) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sr.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(rp.serialNumber) LIKE LOWER(CONCAT('%', :query, '%'))" +
           ")")
    Page<ServiceRequest> searchServiceRequests(
            @Param("companyId") Long companyId,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT sr FROM ServiceRequest sr WHERE (:companyId IS NULL OR sr.companyId = :companyId)")
    Page<ServiceRequest> findAllByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
}
