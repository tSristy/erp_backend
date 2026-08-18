package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {
    List<PurchaseInvoice> findByCompanyId(Long companyId);
    PurchaseInvoice findByInvoiceNoAndCompanyId(String invoiceNo, Long companyId);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM PurchaseInvoice p WHERE p.vendor.id = :vendorId AND p.status = :status AND (p.totalAmount - p.paidAmount) > 0 ORDER BY p.dueDate ASC")
    List<PurchaseInvoice> findUnpaidByVendorOrderByDueDateAsc(@org.springframework.data.repository.query.Param("vendorId") Long vendorId, @org.springframework.data.repository.query.Param("status") PurchaseInvoice.InvoiceStatus status);
}
