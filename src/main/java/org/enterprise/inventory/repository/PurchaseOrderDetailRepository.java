package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Long>, JpaSpecificationExecutor<PurchaseOrderDetail> {
}
