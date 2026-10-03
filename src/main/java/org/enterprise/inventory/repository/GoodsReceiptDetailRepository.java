package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.GoodsReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GoodsReceiptDetailRepository
        extends JpaRepository<GoodsReceiptDetail, Long>, JpaSpecificationExecutor<GoodsReceiptDetail> {
}
