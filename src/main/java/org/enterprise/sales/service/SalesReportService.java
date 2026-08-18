package org.enterprise.sales.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.sales.dto.*;
import org.enterprise.sales.entity.SalesInvoice;
import org.enterprise.sales.repository.SalesInvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesReportService {

    private final SalesInvoiceRepository salesInvoiceRepository;

    private List<SalesInvoice.InvoiceStatus> getValidStatuses() {
        return Arrays.asList(SalesInvoice.InvoiceStatus.POSTED, SalesInvoice.InvoiceStatus.PAID);
    }

    public List<DailySalesReportDto> getDailySalesReport(LocalDate startDate, LocalDate endDate) {
        Long companyId = TenantContext.get().getCompanyId();
        return salesInvoiceRepository.getDailySalesReport(companyId, getValidStatuses(), startDate, endDate);
    }

    public List<CustomerSalesReportDto> getCustomerSalesReport(LocalDate startDate, LocalDate endDate) {
        Long companyId = TenantContext.get().getCompanyId();
        return salesInvoiceRepository.getCustomerSalesReport(companyId, getValidStatuses(), startDate, endDate);
    }

    public List<ProductSalesReportDto> getProductSalesReport(LocalDate startDate, LocalDate endDate) {
        Long companyId = TenantContext.get().getCompanyId();
        return salesInvoiceRepository.getProductSalesReport(companyId, getValidStatuses(), startDate, endDate);
    }

    public List<WarehouseSalesReportDto> getWarehouseSalesReport(LocalDate startDate, LocalDate endDate) {
        Long companyId = TenantContext.get().getCompanyId();
        return salesInvoiceRepository.getWarehouseSalesReport(companyId, getValidStatuses(), startDate, endDate);
    }

    public List<SalespersonReportDto> getSalespersonReport(LocalDate startDate, LocalDate endDate) {
        Long companyId = TenantContext.get().getCompanyId();
        return salesInvoiceRepository.getSalespersonReport(companyId, getValidStatuses(), startDate, endDate);
    }

    public List<ProfitabilityReportDto> getProfitabilityReport(LocalDate startDate, LocalDate endDate) {
        Long companyId = TenantContext.get().getCompanyId();
        return salesInvoiceRepository.getProfitabilityReport(companyId, getValidStatuses(), startDate, endDate);
    }
}
