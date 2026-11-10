package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long>, JpaSpecificationExecutor<PurchaseInvoice> {
    List<PurchaseInvoice> findByCompanyId(Long companyId);
    PurchaseInvoice findByInvoiceNoAndCompanyId(String invoiceNo, Long companyId);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM PurchaseInvoice p WHERE p.vendor.id = :vendorId AND p.status = :status AND (p.totalAmount - p.paidAmount) > 0 ORDER BY p.dueDate ASC")
    List<PurchaseInvoice> findUnpaidByVendorOrderByDueDateAsc(@org.springframework.data.repository.query.Param("vendorId") Long vendorId, @org.springframework.data.repository.query.Param("status") PurchaseInvoice.InvoiceStatus status);

    boolean existsByGoodsReceiptIdAndStatusNot(Long goodsReceiptId, PurchaseInvoice.InvoiceStatus status);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COUNT(p) > 0 FROM PurchaseInvoice p " +
            "LEFT JOIN p.goodsReceipts gr " +
            "LEFT JOIN p.details d " +
            "WHERE p.status <> :status " +
            "AND (p.goodsReceipt.id = :goodsReceiptId OR gr.id = :goodsReceiptId OR d.goodsReceipt.id = :goodsReceiptId) " +
            "AND (:excludeInvoiceId IS NULL OR p.id <> :excludeInvoiceId)"
    )
    boolean isGoodsReceiptInvoiced(
            @org.springframework.data.repository.query.Param("goodsReceiptId") Long goodsReceiptId,
            @org.springframework.data.repository.query.Param("status") PurchaseInvoice.InvoiceStatus status,
            @org.springframework.data.repository.query.Param("excludeInvoiceId") Long excludeInvoiceId
    );
}
