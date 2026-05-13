package org.enterprise.crm.service.repository;

import org.enterprise.crm.service.entity.RegisteredProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegisteredProductRepository extends JpaRepository<RegisteredProduct, Long> {
    List<RegisteredProduct> findByCustomerId(Long customerId);
    List<RegisteredProduct> findBySerialNumber(String serialNumber);
}
