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
    private final org.enterprise.security.repository.UserRepository userRepository;
    private final org.enterprise.hr.repository.EmployeeRepository employeeRepository;

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
        List<SalespersonReportDto> reports = salesInvoiceRepository.getSalespersonReport(companyId, getValidStatuses(), startDate, endDate);

        java.util.Map<String, SalespersonReportDto> merged = new java.util.LinkedHashMap<>();
        for (SalespersonReportDto dto : reports) {
            String name = dto.getSalespersonName();
            if (name == null || name.trim().isEmpty()) {
                if (dto.getSalespersonId() != null) {
                    name = userRepository.findById(dto.getSalespersonId())
                            .map(u -> {
                                if (u.getEmail() != null && !u.getEmail().trim().isEmpty()) {
                                    return employeeRepository.findFirstByEmail(u.getEmail())
                                            .map(org.enterprise.hr.entity.Employee::getFullName)
                                            .orElse(u.getUsername());
                                }
                                return u.getUsername();
                            })
                            .orElse("User #" + dto.getSalespersonId());
                } else {
                    name = "Unassigned";
                }
                dto.setSalespersonName(name);
            }

            String key = (dto.getSalespersonName() != null ? dto.getSalespersonName() : "") + "_" + dto.getSalespersonId();
            if (merged.containsKey(key)) {
                SalespersonReportDto existing = merged.get(key);
                long cnt = (existing.getInvoiceCount() != null ? existing.getInvoiceCount() : 0L) +
                           (dto.getInvoiceCount() != null ? dto.getInvoiceCount() : 0L);
                java.math.BigDecimal rev = (existing.getTotalRevenue() != null ? existing.getTotalRevenue() : java.math.BigDecimal.ZERO)
                        .add(dto.getTotalRevenue() != null ? dto.getTotalRevenue() : java.math.BigDecimal.ZERO);
                existing.setInvoiceCount(cnt);
                existing.setTotalRevenue(rev);
            } else {
                merged.put(key, dto);
            }
        }
        return new java.util.ArrayList<>(merged.values());
    }

    public List<ProfitabilityReportDto> getProfitabilityReport(LocalDate startDate, LocalDate endDate) {
        Long companyId = TenantContext.get().getCompanyId();
        return salesInvoiceRepository.getProfitabilityReport(companyId, getValidStatuses(), startDate, endDate);
    }
}
