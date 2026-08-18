package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.VendorDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VendorDetailRepository extends JpaRepository<VendorDetail, Long> {
    List<VendorDetail> findByCompanyId(Long companyId);
}
