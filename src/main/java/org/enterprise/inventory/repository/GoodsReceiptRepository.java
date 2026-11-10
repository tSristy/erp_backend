package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long>, JpaSpecificationExecutor<GoodsReceipt> {
    @Query("SELECT g FROM GoodsReceipt g WHERE g.status = org.enterprise.inventory.enums.GoodsReceiptStatus.POSTED AND (g.receiptType IS NULL OR g.receiptType = org.enterprise.inventory.entity.GoodsReceipt.ReceiptType.INBOUND_RECEIPT) AND g.id NOT IN (SELECT p.goodsReceipt.id FROM PurchaseInvoice p WHERE p.goodsReceipt IS NOT NULL AND p.status != 'CANCELLED') AND g.id NOT IN (SELECT gr.id FROM PurchaseInvoice p JOIN p.goodsReceipts gr WHERE p.status != 'CANCELLED') AND g.id NOT IN (SELECT d.goodsReceipt.id FROM PurchaseInvoice p JOIN p.details d WHERE d.goodsReceipt IS NOT NULL AND p.status != 'CANCELLED')")
    List<GoodsReceipt> findUninvoiced();
}
