package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySkuAndCompanyId(String sku, Long companyId);
    java.util.List<Product> findByProductType(org.enterprise.inventory.enums.ProductType type);
    
    org.springframework.data.domain.Page<Product> findByCompanyIdAndNameContainingIgnoreCaseOrCompanyIdAndSkuContainingIgnoreCase(
            Long companyId1, String name, Long companyId2, String sku, org.springframework.data.domain.Pageable pageable);
    
    org.springframework.data.domain.Page<Product> findByCompanyId(Long companyId, org.springframework.data.domain.Pageable pageable);
}