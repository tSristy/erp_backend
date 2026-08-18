package org.enterprise.inventory.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.inventory.dto.InventoryTransactionRequest;
import org.enterprise.inventory.dto.StockBalanceResponse;
import org.enterprise.inventory.entity.*;
import org.enterprise.inventory.enums.InventoryTransactionType;
import org.enterprise.inventory.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final LocationRepository locationRepository;
    private final CostingService costingService;

    @Transactional(readOnly = true)
    public List<Inventory> getByProduct(Long productId) {
        Long companyId = TenantContext.getCompanyId();
        return inventoryRepository.findByProductIdAndCompanyId(productId, companyId);
    }

    @Transactional
    public void adjustStock(Long inventoryId, BigDecimal qty) {
        Inventory inv = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        if (!inv.getCompanyId().equals(TenantContext.getCompanyId())) {
            throw new RuntimeException("Unauthorized access to inventory");
        }

        inv.setQuantity(inv.getQuantity().add(qty));
        inventoryRepository.save(inv);
        
        // Note: A true adjustment should also log to InventoryLedger and update StockBalance.
        // For now, retaining the original adjustStock signature but securing it.
    }

    @Transactional
    public void receiveStock(InventoryTransactionRequest request) {
        Long companyId = TenantContext.getCompanyId();

        Product product = productRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        Location location = request.getLocationId() != null 
                ? locationRepository.findById(request.getLocationId()).orElse(null) 
                : null;

        StockBalance balance = stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchId(
                        product.getId(),
                        warehouse.getId(),
                        location != null ? location.getId() : null, null
                )
                .orElseGet(() -> {
                    StockBalance sb = new StockBalance();
                    sb.setProduct(product);
                    sb.setWarehouse(warehouse);
                    sb.setLocation(location);
                    sb.setQuantity(BigDecimal.ZERO);
                    sb.setAverageCost(BigDecimal.ZERO);
                    sb.setTotalValue(BigDecimal.ZERO);
                    sb.setCompanyId(companyId);
                    return sb;
                });

        BigDecimal oldQty = balance.getQuantity() != null ? balance.getQuantity() : BigDecimal.ZERO;
        BigDecimal oldValue = balance.getTotalValue() != null ? balance.getTotalValue() : BigDecimal.ZERO;

        BigDecimal receivedQty = request.getQuantity();
        BigDecimal receivedValue = receivedQty.multiply(request.getUnitCost());

        costingService.addCostLayer(product, warehouse, request.getDocumentType(), request.getDocumentId(), receivedQty, request.getUnitCost());

        BigDecimal newQty = oldQty.add(receivedQty);
        BigDecimal newValue = oldValue.add(receivedValue);

        BigDecimal avgCost = BigDecimal.ZERO;
        if (newQty.compareTo(BigDecimal.ZERO) > 0) {
            avgCost = newValue.divide(newQty, 6, RoundingMode.HALF_UP);
        }

        balance.setQuantity(newQty);
        balance.setAverageCost(avgCost);
        balance.setTotalValue(newValue);
        stockBalanceRepository.save(balance);

        InventoryLedger ledger = new InventoryLedger();
        ledger.setCompanyId(companyId);
        ledger.setProduct(product);
        ledger.setWarehouse(warehouse);
        ledger.setLocation(location);
        ledger.setTransactionType(InventoryTransactionType.valueOf(request.getTransactionType() != null ? request.getTransactionType() : "GRN"));
        ledger.setDocumentType(request.getDocumentType());
        ledger.setDocumentId(request.getDocumentId());
        ledger.setTransactionDate(LocalDateTime.now());
        ledger.setQtyIn(receivedQty);
        ledger.setQtyOut(BigDecimal.ZERO);
        ledger.setUnitCost(request.getUnitCost());
        ledger.setTotalCost(receivedValue);
        ledger.setBalanceQty(newQty);
        ledger.setBalanceCost(newValue);
        inventoryLedgerRepository.save(ledger);
    }

    @Transactional
    public void issueStock(InventoryTransactionRequest request) {
        Long companyId = TenantContext.getCompanyId();

        Product product = productRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        Location location = request.getLocationId() != null 
                ? locationRepository.findById(request.getLocationId()).orElse(null) 
                : null;

        StockBalance balance = stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchId(
                        product.getId(),
                        warehouse.getId(),
                        location != null ? location.getId() : null, null
                )
                .orElseThrow(() -> new RuntimeException("Stock not found for issue"));

        BigDecimal oldQty = balance.getQuantity() != null ? balance.getQuantity() : BigDecimal.ZERO;
        BigDecimal oldValue = balance.getTotalValue() != null ? balance.getTotalValue() : BigDecimal.ZERO;
        BigDecimal issueQty = request.getQuantity();

        if (oldQty.compareTo(issueQty) < 0) {
            throw new RuntimeException("Insufficient stock to issue");
        }

        BigDecimal issueValue = costingService.consumeCost(product, warehouse, issueQty);
        BigDecimal unitCost = issueValue.divide(issueQty, 6, RoundingMode.HALF_UP);

        BigDecimal newQty = oldQty.subtract(issueQty);
        BigDecimal newValue = oldValue.subtract(issueValue);

        balance.setQuantity(newQty);
        balance.setTotalValue(newValue);
        if (newQty.compareTo(BigDecimal.ZERO) == 0) {
            balance.setAverageCost(BigDecimal.ZERO);
            balance.setTotalValue(BigDecimal.ZERO);
        }
        stockBalanceRepository.save(balance);

        InventoryLedger ledger = new InventoryLedger();
        ledger.setCompanyId(companyId);
        ledger.setProduct(product);
        ledger.setWarehouse(warehouse);
        ledger.setLocation(location);
        ledger.setTransactionType(InventoryTransactionType.valueOf(request.getTransactionType() != null ? request.getTransactionType() : "GOODS_ISSUE"));
        ledger.setDocumentType(request.getDocumentType());
        ledger.setDocumentId(request.getDocumentId());
        ledger.setTransactionDate(LocalDateTime.now());
        ledger.setQtyIn(BigDecimal.ZERO);
        ledger.setQtyOut(issueQty);
        ledger.setUnitCost(unitCost);
        ledger.setTotalCost(issueValue);
        ledger.setBalanceQty(newQty);
        ledger.setBalanceCost(newValue);
        inventoryLedgerRepository.save(ledger);
    }

    @Transactional(readOnly = true)
    public StockBalanceResponse getStockBalance(Long itemId, Long warehouseId, Long locationId) {
        StockBalance balance = stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchId(
                        itemId,
                        warehouseId,
                        locationId, null
                )
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        if (!balance.getCompanyId().equals(TenantContext.getCompanyId())) {
            throw new RuntimeException("Unauthorized access to stock balance");
        }

        return new StockBalanceResponse(
                balance.getProduct().getId(),
                balance.getWarehouse().getId(),
                balance.getLocation() != null ? balance.getLocation().getId() : null,
                balance.getQuantity(),
                balance.getAverageCost(),
                balance.getTotalValue()
        );
    }
}