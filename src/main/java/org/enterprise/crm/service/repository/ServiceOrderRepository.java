package org.enterprise.crm.service.repository;

import org.enterprise.crm.service.entity.ServiceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
    List<ServiceOrder> findByServiceRequestId(Long serviceRequestId);

    Optional<ServiceOrder> findTopByOrderNumberStartingWithOrderByIdDesc(String prefix);

    Optional<ServiceOrder> findTopByOrderByIdDesc();

    boolean existsByOrderNumber(String orderNumber);

    @Query("SELECT so FROM ServiceOrder so " +
           "LEFT JOIN so.serviceRequest sr " +
           "WHERE (:companyId IS NULL OR so.companyId = :companyId) AND (" +
           "LOWER(so.orderNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sr.requestNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(so.technicianNotes) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(so.qcNotes) LIKE LOWER(CONCAT('%', :query, '%'))" +
           ")")
    Page<ServiceOrder> searchServiceOrders(
            @Param("companyId") Long companyId,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT so FROM ServiceOrder so WHERE (:companyId IS NULL OR so.companyId = :companyId)")
    Page<ServiceOrder> findAllByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
}
