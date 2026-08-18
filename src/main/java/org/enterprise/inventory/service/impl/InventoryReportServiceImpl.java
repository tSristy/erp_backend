package org.enterprise.inventory.service.impl;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.service.InventoryReportService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryReportServiceImpl implements InventoryReportService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getMovementRegister(Long companyId, Long warehouseId, Long locationId, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder(
                "SELECT l.transaction_date as \"date\", p.sku as \"sku\", p.name as \"productName\", " +
                "w.name as \"warehouseName\", loc.name as \"locationName\", " +
                "l.transaction_type as \"type\", l.document_type as \"docType\", l.document_id as \"docId\", " +
                "l.qty_in as \"qtyIn\", l.qty_out as \"qtyOut\", l.balance_qty as \"balanceQty\" " +
                "FROM inv_inventory_ledger l " +
                "JOIN products p ON l.product_id = p.id " +
                "LEFT JOIN warehouses w ON l.warehouse_id = w.id " +
                "LEFT JOIN locations loc ON l.location_id = loc.id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (companyId != null) { sql.append(" AND l.company_id = ? "); params.add(companyId); }
        if (warehouseId != null) { sql.append(" AND l.warehouse_id = ? "); params.add(warehouseId); }
        if (locationId != null) { sql.append(" AND l.location_id = ? "); params.add(locationId); }
        if (startDate != null) { sql.append(" AND l.transaction_date >= ? "); params.add(startDate.atStartOfDay()); }
        if (endDate != null) { sql.append(" AND l.transaction_date <= ? "); params.add(endDate.atTime(23, 59, 59)); }

        sql.append(" ORDER BY l.transaction_date DESC, l.id DESC");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Override
    public List<Map<String, Object>> getInventoryStock(Long companyId, Long warehouseId, Long locationId, LocalDate asOfDate) {
        // Simple stock balance query for current stock
        // If asOfDate is provided, historical stock calculation is needed, but for simplicity, we return current stock if asOfDate is null
        StringBuilder sql = new StringBuilder(
                "SELECT p.sku as \"sku\", p.name as \"productName\", w.name as \"warehouseName\", loc.name as \"locationName\", " +
                "SUM(sb.quantity) as \"quantity\", MAX(sb.average_cost) as \"averageCost\", SUM(sb.total_value) as \"totalValue\" " +
                "FROM inv_stock_balances sb " +
                "JOIN products p ON sb.product_id = p.id " +
                "LEFT JOIN warehouses w ON sb.warehouse_id = w.id " +
                "LEFT JOIN locations loc ON sb.location_id = loc.id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (companyId != null) { sql.append(" AND sb.company_id = ? "); params.add(companyId); }
        if (warehouseId != null) { sql.append(" AND sb.warehouse_id = ? "); params.add(warehouseId); }
        if (locationId != null) { sql.append(" AND sb.location_id = ? "); params.add(locationId); }
        // asOfDate requires complex ledger reconstruction; skipping for standard current stock view
        
        sql.append(" GROUP BY p.sku, p.name, w.name, loc.name HAVING SUM(sb.quantity) > 0");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Override
    public List<Map<String, Object>> getInventoryAging(Long companyId, Long warehouseId, Long locationId, LocalDate asOfDate) {
        // Querying from inv_cost_layers where remaining_qty > 0
        StringBuilder sql = new StringBuilder(
                "SELECT p.sku as \"sku\", p.name as \"productName\", w.name as \"warehouseName\", " +
                "cl.receipt_date as \"receiptDate\", cl.unit_cost as \"unitCost\", cl.remaining_qty as \"remainingQty\", " +
                "EXTRACT(DAY FROM (COALESCE(?, CURRENT_DATE) - cl.receipt_date)) as \"ageInDays\" " +
                "FROM inv_cost_layers cl " +
                "JOIN products p ON cl.product_id = p.id " +
                "LEFT JOIN warehouses w ON cl.warehouse_id = w.id " +
                "WHERE cl.remaining_qty > 0 "
        );
        List<Object> params = new ArrayList<>();
        params.add(asOfDate != null ? java.sql.Date.valueOf(asOfDate) : null);

        if (companyId != null) { sql.append(" AND cl.company_id = ? "); params.add(companyId); }
        if (warehouseId != null) { sql.append(" AND cl.warehouse_id = ? "); params.add(warehouseId); }
        // Note: Cost layers do not typically track location, only warehouse. Assuming no locationId filter here.

        sql.append(" ORDER BY cl.receipt_date ASC");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Override
    public List<Map<String, Object>> getInventoryValuation(Long companyId, Long warehouseId, Long locationId, LocalDate asOfDate) {
        // Similar to stock balance but focusing on valuation
        StringBuilder sql = new StringBuilder(
                "SELECT p.sku as \"sku\", p.name as \"productName\", w.name as \"warehouseName\", " +
                "SUM(sb.quantity) as \"totalQuantity\", SUM(sb.total_value) as \"totalValue\" " +
                "FROM inv_stock_balances sb " +
                "JOIN products p ON sb.product_id = p.id " +
                "LEFT JOIN warehouses w ON sb.warehouse_id = w.id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (companyId != null) { sql.append(" AND sb.company_id = ? "); params.add(companyId); }
        if (warehouseId != null) { sql.append(" AND sb.warehouse_id = ? "); params.add(warehouseId); }
        if (locationId != null) { sql.append(" AND sb.location_id = ? "); params.add(locationId); }

        sql.append(" GROUP BY p.sku, p.name, w.name HAVING SUM(sb.quantity) > 0");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Override
    public List<Map<String, Object>> getCurrentStockSerialNo(Long companyId, Long warehouseId, Long locationId) {
        StringBuilder sql = new StringBuilder(
                "SELECT sn.serial_no as \"serialNo\", p.sku as \"sku\", p.name as \"productName\", " +
                "w.name as \"warehouseName\", loc.name as \"locationName\", sn.status as \"status\" " +
                "FROM inv_serial_numbers sn " +
                "JOIN products p ON sn.product_id = p.id " +
                "LEFT JOIN warehouses w ON sn.warehouse_id = w.id " +
                "LEFT JOIN locations loc ON sn.location_id = loc.id " +
                "WHERE sn.status = 'IN_STOCK' "
        );
        List<Object> params = new ArrayList<>();

        if (companyId != null) { sql.append(" AND sn.company_id = ? "); params.add(companyId); }
        if (warehouseId != null) { sql.append(" AND sn.warehouse_id = ? "); params.add(warehouseId); }
        if (locationId != null) { sql.append(" AND sn.location_id = ? "); params.add(locationId); }

        sql.append(" ORDER BY sn.created_at DESC");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Override
    public List<Map<String, Object>> getPurchaseSummary(Long companyId, Long warehouseId, Long locationId, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder(
                "SELECT po.po_no as \"poNo\", po.po_date as \"poDate\", bp.name as \"vendorName\", " +
                "po.status as \"status\", po.total_amount as \"totalAmount\", po.currency as \"currency\" " +
                "FROM pur_purchase_orders po " +
                "LEFT JOIN business_partners bp ON po.vendor_id = bp.id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (companyId != null) { sql.append(" AND po.company_id = ? "); params.add(companyId); }
        if (warehouseId != null) { sql.append(" AND po.warehouse_id = ? "); params.add(warehouseId); }
        // Purchase orders don't track location natively, usually just warehouse
        if (startDate != null) { sql.append(" AND po.po_date >= ? "); params.add(startDate); }
        if (endDate != null) { sql.append(" AND po.po_date <= ? "); params.add(endDate); }

        sql.append(" ORDER BY po.po_date DESC");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }
}
