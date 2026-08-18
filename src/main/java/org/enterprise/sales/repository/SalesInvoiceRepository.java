package org.enterprise.sales.repository;

import org.enterprise.sales.entity.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {
    List<SalesInvoice> findByCompanyId(Long companyId);
    
    @org.springframework.data.jpa.repository.Query("SELECT s FROM SalesInvoice s WHERE s.customer.id = :customerId AND s.status = :status AND (s.totalAmount - s.paidAmount) > 0 ORDER BY s.dueDate ASC")
    List<SalesInvoice> findUnpaidByCustomerOrderByDueDateAsc(@org.springframework.data.repository.query.Param("customerId") Long customerId, @org.springframework.data.repository.query.Param("status") SalesInvoice.InvoiceStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT new org.enterprise.sales.dto.DailySalesReportDto(" +
            "s.invoiceDate, COUNT(s.id), SUM(s.totalAmount), SUM(s.discountTotal)) " +
            "FROM SalesInvoice s " +
            "WHERE s.companyId = :companyId AND s.status IN :statuses " +
            "AND (cast(:startDate as date) IS NULL OR s.invoiceDate >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR s.invoiceDate <= :endDate) " +
            "GROUP BY s.invoiceDate ORDER BY s.invoiceDate ASC")
    List<org.enterprise.sales.dto.DailySalesReportDto> getDailySalesReport(
            @org.springframework.data.repository.query.Param("companyId") Long companyId,
            @org.springframework.data.repository.query.Param("statuses") List<SalesInvoice.InvoiceStatus> statuses,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT new org.enterprise.sales.dto.CustomerSalesReportDto(" +
            "s.customer.id, s.customer.name, COUNT(s.id), SUM(s.totalAmount)) " +
            "FROM SalesInvoice s " +
            "WHERE s.companyId = :companyId AND s.status IN :statuses " +
            "AND (cast(:startDate as date) IS NULL OR s.invoiceDate >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR s.invoiceDate <= :endDate) " +
            "GROUP BY s.customer.id, s.customer.name ORDER BY SUM(s.totalAmount) DESC")
    List<org.enterprise.sales.dto.CustomerSalesReportDto> getCustomerSalesReport(
            @org.springframework.data.repository.query.Param("companyId") Long companyId,
            @org.springframework.data.repository.query.Param("statuses") List<SalesInvoice.InvoiceStatus> statuses,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT new org.enterprise.sales.dto.WarehouseSalesReportDto(" +
            "s.warehouse.id, s.warehouse.code, s.warehouse.name, COUNT(s.id), SUM(s.totalAmount)) " +
            "FROM SalesInvoice s " +
            "WHERE s.companyId = :companyId AND s.status IN :statuses " +
            "AND (cast(:startDate as date) IS NULL OR s.invoiceDate >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR s.invoiceDate <= :endDate) " +
            "GROUP BY s.warehouse.id, s.warehouse.code, s.warehouse.name ORDER BY SUM(s.totalAmount) DESC")
    List<org.enterprise.sales.dto.WarehouseSalesReportDto> getWarehouseSalesReport(
            @org.springframework.data.repository.query.Param("companyId") Long companyId,
            @org.springframework.data.repository.query.Param("statuses") List<SalesInvoice.InvoiceStatus> statuses,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT new org.enterprise.sales.dto.SalespersonReportDto(" +
            "s.createdBy, COUNT(s.id), SUM(s.totalAmount)) " +
            "FROM SalesInvoice s " +
            "WHERE s.companyId = :companyId AND s.status IN :statuses " +
            "AND (cast(:startDate as date) IS NULL OR s.invoiceDate >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR s.invoiceDate <= :endDate) " +
            "GROUP BY s.createdBy ORDER BY SUM(s.totalAmount) DESC")
    List<org.enterprise.sales.dto.SalespersonReportDto> getSalespersonReport(
            @org.springframework.data.repository.query.Param("companyId") Long companyId,
            @org.springframework.data.repository.query.Param("statuses") List<SalesInvoice.InvoiceStatus> statuses,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT new org.enterprise.sales.dto.ProductSalesReportDto(" +
            "d.product.id, d.product.sku, d.product.name, SUM(d.quantity), SUM(d.lineTotal)) " +
            "FROM SalesInvoiceDetail d JOIN d.salesInvoice s " +
            "WHERE s.companyId = :companyId AND s.status IN :statuses " +
            "AND (cast(:startDate as date) IS NULL OR s.invoiceDate >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR s.invoiceDate <= :endDate) " +
            "GROUP BY d.product.id, d.product.sku, d.product.name ORDER BY SUM(d.lineTotal) DESC")
    List<org.enterprise.sales.dto.ProductSalesReportDto> getProductSalesReport(
            @org.springframework.data.repository.query.Param("companyId") Long companyId,
            @org.springframework.data.repository.query.Param("statuses") List<SalesInvoice.InvoiceStatus> statuses,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT new org.enterprise.sales.dto.ProfitabilityReportDto(" +
            "d.product.id, d.product.sku, d.product.name, SUM(d.quantity), SUM(d.lineTotal), " +
            "SUM(COALESCE(d.deliveryNoteDetail.unitCost, 0) * d.quantity), " +
            "SUM(d.lineTotal) - SUM(COALESCE(d.deliveryNoteDetail.unitCost, 0) * d.quantity)) " +
            "FROM SalesInvoiceDetail d JOIN d.salesInvoice s " +
            "WHERE s.companyId = :companyId AND s.status IN :statuses " +
            "AND (cast(:startDate as date) IS NULL OR s.invoiceDate >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR s.invoiceDate <= :endDate) " +
            "GROUP BY d.product.id, d.product.sku, d.product.name ORDER BY (SUM(d.lineTotal) - SUM(COALESCE(d.deliveryNoteDetail.unitCost, 0) * d.quantity)) DESC")
    List<org.enterprise.sales.dto.ProfitabilityReportDto> getProfitabilityReport(
            @org.springframework.data.repository.query.Param("companyId") Long companyId,
            @org.springframework.data.repository.query.Param("statuses") List<SalesInvoice.InvoiceStatus> statuses,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);
}
