package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySkuAndCompanyId(String sku, Long companyId);
}