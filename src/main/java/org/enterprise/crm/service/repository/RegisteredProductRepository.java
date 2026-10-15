package org.enterprise.crm.service.repository;

import org.enterprise.crm.service.entity.RegisteredProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegisteredProductRepository extends JpaRepository<RegisteredProduct, Long> {
    List<RegisteredProduct> findByCustomerId(Long customerId);
    List<RegisteredProduct> findBySerialNumber(String serialNumber);

    @Query("SELECT rp FROM RegisteredProduct rp " +
           "LEFT JOIN rp.customer c " +
           "LEFT JOIN rp.product p " +
           "LEFT JOIN rp.variant v " +
           "WHERE (:companyId IS NULL OR rp.companyId = :companyId) AND (" +
           "LOWER(rp.serialNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(rp.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(rp.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.sku) LIKE LOWER(CONCAT('%', :query, '%'))" +
           ")")
    Page<RegisteredProduct> searchRegisteredProducts(
            @Param("companyId") Long companyId,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT rp FROM RegisteredProduct rp WHERE (:companyId IS NULL OR rp.companyId = :companyId)")
    Page<RegisteredProduct> findAllByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
}
