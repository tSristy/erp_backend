package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.VendorDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface VendorDetailRepository extends JpaRepository<VendorDetail, Long>, JpaSpecificationExecutor<VendorDetail> {
    List<VendorDetail> findByCompanyId(Long companyId);
}
