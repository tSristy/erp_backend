package org.enterprise.inventory.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface InventoryReportService {

    List<Map<String, Object>> getMovementRegister(Long companyId, Long warehouseId, Long locationId, LocalDate startDate, LocalDate endDate);

    List<Map<String, Object>> getInventoryStock(Long companyId, Long warehouseId, Long locationId, LocalDate asOfDate);

    List<Map<String, Object>> getInventoryAging(Long companyId, Long warehouseId, Long locationId, LocalDate asOfDate);

    List<Map<String, Object>> getInventoryValuation(Long companyId, Long warehouseId, Long locationId, LocalDate asOfDate);

    List<Map<String, Object>> getCurrentStockSerialNo(Long companyId, Long warehouseId, Long locationId);

    List<Map<String, Object>> getPurchaseSummary(Long companyId, Long warehouseId, Long locationId, LocalDate startDate, LocalDate endDate);
}
