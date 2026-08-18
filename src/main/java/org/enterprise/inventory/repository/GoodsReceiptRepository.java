package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    @Query("SELECT g FROM GoodsReceipt g WHERE g.id NOT IN (SELECT p.goodsReceipt.id FROM PurchaseInvoice p WHERE p.goodsReceipt IS NOT NULL AND p.status != 'CANCELLED')")
    List<GoodsReceipt> findUninvoiced();
}
