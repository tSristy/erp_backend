package org.enterprise.crm.service.repository;

import org.enterprise.crm.service.entity.ServicePartsRequisition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePartsRequisitionRepository extends JpaRepository<ServicePartsRequisition, Long> {
    List<ServicePartsRequisition> findByServiceOrderId(Long serviceOrderId);

    @Query("SELECT spr FROM ServicePartsRequisition spr " +
           "LEFT JOIN spr.serviceOrder so " +
           "LEFT JOIN spr.warehouse w " +
           "WHERE (:companyId IS NULL OR spr.companyId = :companyId) AND (" +
           "LOWER(COALESCE(so.orderNumber, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(w.name, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CAST(spr.status AS string)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CAST(spr.id AS string)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CAST(spr.requestedById AS string)) LIKE LOWER(CONCAT('%', :query, '%'))" +
           ")")
    Page<ServicePartsRequisition> searchServicePartsRequisitions(
            @Param("companyId") Long companyId,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT spr FROM ServicePartsRequisition spr WHERE (:companyId IS NULL OR spr.companyId = :companyId)")
    Page<ServicePartsRequisition> findAllByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
}
