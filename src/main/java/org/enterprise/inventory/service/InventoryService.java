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

        if (companyId == null) {
            companyId = warehouse.getCompanyId() != null ? warehouse.getCompanyId() : product.getCompanyId();
        }
        final Long resolvedCompanyId = companyId;

        Location location = request.getLocationId() != null 
                ? locationRepository.findById(request.getLocationId()).orElse(null) 
                : null;

        StockBalance balance = stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchIsNull(
                        product.getId(),
                        warehouse.getId(),
                        location != null ? location.getId() : null
                )
                .orElseGet(() -> {
                    StockBalance sb = new StockBalance();
                    sb.setProduct(product);
                    sb.setWarehouse(warehouse);
                    sb.setLocation(location);
                    sb.setQuantity(BigDecimal.ZERO);
                    sb.setAverageCost(BigDecimal.ZERO);
                    sb.setTotalValue(BigDecimal.ZERO);
                    sb.setCompanyId(resolvedCompanyId);
                    return sb;
                });

        BigDecimal oldQty = balance.getQuantity() != null ? balance.getQuantity() : BigDecimal.ZERO;
        BigDecimal oldValue = balance.getTotalValue() != null ? balance.getTotalValue() : BigDecimal.ZERO;

        BigDecimal receivedQty = request.getQuantity();
        BigDecimal unitCost = request.getUnitCost() != null ? request.getUnitCost() : BigDecimal.ZERO;
        BigDecimal receivedValue = receivedQty.multiply(unitCost);

        costingService.addCostLayer(product, warehouse, request.getDocumentType(), request.getDocumentId(), receivedQty, unitCost);

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
        ledger.setCompanyId(resolvedCompanyId);
        ledger.setProduct(product);
        ledger.setWarehouse(warehouse);
        ledger.setLocation(location);
        ledger.setTransactionType(InventoryTransactionType.valueOf(request.getTransactionType() != null ? request.getTransactionType() : "GRN"));
        ledger.setDocumentType(request.getDocumentType());
        ledger.setDocumentId(request.getDocumentId());
        ledger.setTransactionDate(LocalDateTime.now());
        ledger.setQtyIn(receivedQty);
        ledger.setQtyOut(BigDecimal.ZERO);
        ledger.setUnitCost(unitCost);
        ledger.setTotalCost(receivedValue);
        ledger.setBalanceQty(newQty);
        ledger.setBalanceCost(newValue);
        inventoryLedgerRepository.save(ledger);
    }

    @Transactional
    public BigDecimal issueStock(InventoryTransactionRequest request) {
        Long companyId = TenantContext.getCompanyId();

        Product product = productRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        if (companyId == null) {
            companyId = warehouse.getCompanyId() != null ? warehouse.getCompanyId() : product.getCompanyId();
        }
        final Long resolvedCompanyId = companyId;

        Location location = request.getLocationId() != null 
                ? locationRepository.findById(request.getLocationId()).orElse(null) 
                : null;

        BigDecimal issueQty = request.getQuantity();
        boolean isPosTransaction = "POS_TRANSACTION".equalsIgnoreCase(request.getDocumentType());

        StockBalance balance = stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchIsNull(
                        product.getId(),
                        warehouse.getId(),
                        location != null ? location.getId() : null
                )
                .filter(sb -> sb.getQuantity() != null && sb.getQuantity().compareTo(issueQty) >= 0)
                .or(() -> stockBalanceRepository.findByProductIdAndWarehouseIdOrderByQuantityDesc(product.getId(), warehouse.getId())
                        .stream()
                        .findFirst())
                .orElseGet(() -> {
                    if (!isPosTransaction) {
                        throw new RuntimeException("Stock not found for issue");
                    }
                    StockBalance sb = new StockBalance();
                    sb.setProduct(product);
                    sb.setWarehouse(warehouse);
                    sb.setLocation(location);
                    sb.setQuantity(BigDecimal.ZERO);
                    sb.setAverageCost(request.getUnitCost() != null ? request.getUnitCost() : BigDecimal.ZERO);
                    sb.setTotalValue(BigDecimal.ZERO);
                    sb.setCompanyId(resolvedCompanyId);
                    return sb;
                });

        BigDecimal oldQty = balance.getQuantity() != null ? balance.getQuantity() : BigDecimal.ZERO;
        BigDecimal oldValue = balance.getTotalValue() != null ? balance.getTotalValue() : BigDecimal.ZERO;

        if (!isPosTransaction && oldQty.compareTo(issueQty) < 0) {
            throw new RuntimeException("Insufficient stock to issue");
        }

        BigDecimal issueValue = costingService.consumeCost(product, warehouse, issueQty);
        if ((issueValue == null || issueValue.compareTo(BigDecimal.ZERO) <= 0)
                && balance.getAverageCost() != null && balance.getAverageCost().compareTo(BigDecimal.ZERO) > 0) {
            issueValue = balance.getAverageCost().multiply(issueQty);
        }
        if ((issueValue == null || issueValue.compareTo(BigDecimal.ZERO) <= 0)
                && request.getUnitCost() != null && request.getUnitCost().compareTo(BigDecimal.ZERO) > 0) {
            issueValue = request.getUnitCost().multiply(issueQty);
        }
        if (issueValue == null) {
            issueValue = BigDecimal.ZERO;
        }
        BigDecimal unitCost = issueQty.compareTo(BigDecimal.ZERO) > 0
                ? issueValue.divide(issueQty, 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal newQty = oldQty.subtract(issueQty);
        BigDecimal newValue = oldValue.subtract(issueValue);
        if (isPosTransaction && newQty.compareTo(BigDecimal.ZERO) < 0) {
            newQty = BigDecimal.ZERO;
            newValue = BigDecimal.ZERO;
        }

        balance.setQuantity(newQty);
        balance.setTotalValue(newValue.max(BigDecimal.ZERO));
        if (newQty.compareTo(BigDecimal.ZERO) <= 0) {
            balance.setTotalValue(BigDecimal.ZERO);
        }
        stockBalanceRepository.save(balance);

        InventoryLedger ledger = new InventoryLedger();
        ledger.setCompanyId(resolvedCompanyId);
        ledger.setProduct(product);
        ledger.setWarehouse(warehouse);
        ledger.setLocation(balance.getLocation() != null ? balance.getLocation() : location);
        ledger.setTransactionType(InventoryTransactionType.valueOf(request.getTransactionType() != null ? request.getTransactionType() : "ISSUE"));
        ledger.setDocumentType(request.getDocumentType());
        ledger.setDocumentId(request.getDocumentId());
        ledger.setTransactionDate(LocalDateTime.now());
        ledger.setQtyIn(BigDecimal.ZERO);
        ledger.setQtyOut(issueQty);
        ledger.setUnitCost(unitCost);
        ledger.setTotalCost(issueValue);
        ledger.setBalanceQty(newQty);
        ledger.setBalanceCost(balance.getTotalValue());
        inventoryLedgerRepository.save(ledger);
        return issueValue;
    }

    @Transactional(readOnly = true)
    public StockBalanceResponse getStockBalance(Long itemId, Long warehouseId, Long locationId) {
        StockBalance balance = stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchIsNull(
                        itemId,
                        warehouseId,
                        locationId
                )
                .orElse(null);

        if (balance == null) {
            return new StockBalanceResponse(
                    itemId,
                    warehouseId,
                    locationId,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

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


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.InventoryLedger> searchInventoryLedgers(String query, org.springframework.data.domain.Pageable pageable) {
        return inventoryLedgerRepository.findAll(org.enterprise.inventory.specification.InventoryLedgerSpecification.searchByQuery(query), pageable);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.StockBalance> searchStockBalances(String query, org.springframework.data.domain.Pageable pageable) {
        return stockBalanceRepository.findAll(org.enterprise.inventory.specification.StockBalanceSpecification.searchByQuery(query), pageable);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Product> searchProducts(String query, org.springframework.data.domain.Pageable pageable) {
        return productRepository.findAll(org.enterprise.inventory.specification.ProductSpecification.searchByQuery(query), pageable);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Inventory> searchInventorys(String query, org.springframework.data.domain.Pageable pageable) {
        return inventoryRepository.findAll(org.enterprise.inventory.specification.InventorySpecification.searchByQuery(query), pageable);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Warehouse> searchWarehouses(String query, org.springframework.data.domain.Pageable pageable) {
        return warehouseRepository.findAll(org.enterprise.inventory.specification.WarehouseSpecification.searchByQuery(query), pageable);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Location> searchLocations(String query, org.springframework.data.domain.Pageable pageable) {
        return locationRepository.findAll(org.enterprise.inventory.specification.LocationSpecification.searchByQuery(query), pageable);
    }
}