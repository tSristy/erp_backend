package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findByCompanyId(Long companyId);
}
