package org.enterprise.production.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.production.dto.*;
import org.enterprise.production.entity.ManufacturingOrder;
import org.enterprise.production.repository.ManufacturingOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionReportService {

    private final ManufacturingOrderRepository orderRepository;

    private List<ManufacturingOrder.OrderStatus> getValidStatuses() {
        return Arrays.asList(ManufacturingOrder.OrderStatus.COMPLETED, ManufacturingOrder.OrderStatus.IN_PROGRESS);
    }

    public List<DailyProductionReportDto> getDailyProductionReport(LocalDate startDate, LocalDate endDate) {
        Long companyId = TenantContext.get().getCompanyId();
        return orderRepository.getDailyProductionReport(companyId, getValidStatuses(), startDate, endDate);
    }

    public List<ProductProductionReportDto> getProductProductionReport(LocalDate startDate, LocalDate endDate) {
        Long companyId = TenantContext.get().getCompanyId();
        return orderRepository.getProductProductionReport(companyId, getValidStatuses(), startDate, endDate);
    }

    public List<ProductionStatusReportDto> getProductionStatusReport(LocalDate startDate, LocalDate endDate) {
        Long companyId = TenantContext.get().getCompanyId();
        return orderRepository.getProductionStatusReport(companyId, startDate, endDate);
    }

    public List<ProductionYieldReportDto> getProductionYieldReport(LocalDate startDate, LocalDate endDate) {
        Long companyId = TenantContext.get().getCompanyId();
        return orderRepository.getProductionYieldReport(companyId, ManufacturingOrder.OrderStatus.COMPLETED, startDate, endDate);
    }

    public List<BomUsageReportDto> getBomUsageReport(LocalDate startDate, LocalDate endDate) {
        Long companyId = TenantContext.get().getCompanyId();
        return orderRepository.getBomUsageReport(companyId, getValidStatuses(), startDate, endDate);
    }
}
