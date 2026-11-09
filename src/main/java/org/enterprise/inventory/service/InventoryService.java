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
    private final BatchRepository batchRepository;
    private final SerialNumberRepository serialNumberRepository;
    private final CostingService costingService;

    private List<String> collectSerialNumbers(InventoryTransactionRequest request) {
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        if (request.getSerialNumbers() != null) {
            for (String sn : request.getSerialNumbers()) {
                if (sn != null && !sn.trim().isEmpty()) {
                    set.add(sn.trim());
                }
            }
        }
        if (request.getSerialNo() != null && !request.getSerialNo().trim().isEmpty()) {
            set.add(request.getSerialNo().trim());
        }
        return new java.util.ArrayList<>(set);
    }

    private Batch resolveBatchFromRequest(InventoryTransactionRequest request, Long productId) {
        if (request.getBatchId() != null) {
            Batch byId = batchRepository.findById(request.getBatchId()).orElse(null);
            if (byId != null) return byId;
        }
        if (request.getBatchNo() != null && !request.getBatchNo().trim().isEmpty()) {
            return batchRepository.findByBatchNoAndProductId(request.getBatchNo().trim(), productId).orElse(null);
        }
        return null;
    }

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

        Batch resolvedBatch = resolveBatchFromRequest(request, product.getId());
        List<String> serials = collectSerialNumbers(request);
        for (String sn : serials) {
            SerialNumber serialEntity = serialNumberRepository.findBySerialNoAndProductId(sn, product.getId())
                    .orElse(null);
            if (serialEntity != null) {
                serialEntity.setStatus(SerialNumber.SerialStatus.IN_STOCK);
                serialEntity.setWarehouse(warehouse);
                if (location != null) {
                    serialEntity.setLocation(location);
                } else if (serialEntity.getLocation() != null) {
                    location = serialEntity.getLocation();
                }
                if (resolvedBatch == null && serialEntity.getBatch() != null) {
                    resolvedBatch = serialEntity.getBatch();
                } else if (resolvedBatch != null && serialEntity.getBatch() == null) {
                    serialEntity.setBatch(resolvedBatch);
                }
                serialNumberRepository.save(serialEntity);
            } else {
                SerialNumber newSerial = new SerialNumber();
                newSerial.setCompanyId(resolvedCompanyId);
                newSerial.setSerialNo(sn);
                newSerial.setProduct(product);
                newSerial.setWarehouse(warehouse);
                newSerial.setLocation(location);
                newSerial.setBatch(resolvedBatch);
                newSerial.setStatus(SerialNumber.SerialStatus.IN_STOCK);
                serialNumberRepository.save(newSerial);
            }
        }

        if (resolvedBatch == null && Boolean.TRUE.equals(product.getIsBatchManaged())) {
            resolvedBatch = stockBalanceRepository.findByProductIdAndWarehouseIdOrderByQuantityDesc(product.getId(), warehouse.getId())
                    .stream()
                    .map(StockBalance::getBatch)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        final Location finalLocation = location;
        final Batch finalBatch = resolvedBatch;

        StockBalance balance = (finalBatch != null
                ? stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchId(
                        product.getId(),
                        warehouse.getId(),
                        finalLocation != null ? finalLocation.getId() : null,
                        finalBatch.getId()
                  ).or(() -> stockBalanceRepository.findByProductIdAndWarehouseIdOrderByQuantityDesc(product.getId(), warehouse.getId())
                        .stream()
                        .filter(sb -> sb.getBatch() != null && sb.getBatch().getId().equals(finalBatch.getId()))
                        .findFirst())
                : stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchIsNull(
                        product.getId(),
                        warehouse.getId(),
                        finalLocation != null ? finalLocation.getId() : null
                  ))
                .orElseGet(() -> {
                    StockBalance sb = new StockBalance();
                    sb.setProduct(product);
                    sb.setWarehouse(warehouse);
                    sb.setLocation(finalLocation);
                    sb.setBatch(finalBatch);
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
        if (balance.getBatch() == null && finalBatch != null) {
            balance.setBatch(finalBatch);
        }
        stockBalanceRepository.save(balance);

        InventoryLedger ledger = new InventoryLedger();
        ledger.setCompanyId(resolvedCompanyId);
        ledger.setProduct(product);
        ledger.setWarehouse(warehouse);
        ledger.setLocation(balance.getLocation() != null ? balance.getLocation() : finalLocation);
        ledger.setBatch(balance.getBatch() != null ? balance.getBatch() : finalBatch);
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

        Batch resolvedBatch = resolveBatchFromRequest(request, product.getId());
        List<String> serials = collectSerialNumbers(request);

        for (String sn : serials) {
            SerialNumber serialEntity = serialNumberRepository.findBySerialNoAndProductId(sn, product.getId())
                    .orElse(null);
            if (serialEntity != null) {
                serialEntity.setStatus(SerialNumber.SerialStatus.ISSUED);
                if (resolvedBatch == null && serialEntity.getBatch() != null) {
                    resolvedBatch = serialEntity.getBatch();
                }
                if (location == null && serialEntity.getLocation() != null) {
                    location = serialEntity.getLocation();
                }
                serialNumberRepository.save(serialEntity);
            }
        }

        // If serial-managed POS sale did not specify serial numbers explicitly, auto-issue available IN_STOCK serials in that warehouse
        if (serials.isEmpty() && isPosTransaction && Boolean.TRUE.equals(product.getIsSerialManaged())) {
            int countToIssue = issueQty != null ? issueQty.intValue() : 0;
            if (countToIssue > 0) {
                List<SerialNumber> availableSerials = serialNumberRepository.findByProductIdAndWarehouseIdAndStatusOrderByIdAsc(
                        product.getId(), warehouse.getId(), SerialNumber.SerialStatus.IN_STOCK);
                for (int i = 0; i < Math.min(countToIssue, availableSerials.size()); i++) {
                    SerialNumber snEntity = availableSerials.get(i);
                    snEntity.setStatus(SerialNumber.SerialStatus.ISSUED);
                    if (resolvedBatch == null && snEntity.getBatch() != null) {
                        resolvedBatch = snEntity.getBatch();
                    }
                    if (location == null && snEntity.getLocation() != null) {
                        location = snEntity.getLocation();
                    }
                    serialNumberRepository.save(snEntity);
                }
            }
        }

        // If batch-managed product has no explicit batch, pick FEFO (First Expiry, First Out) batch
        if (resolvedBatch == null && Boolean.TRUE.equals(product.getIsBatchManaged())) {
            resolvedBatch = stockBalanceRepository.findAvailableBatchesForIssue(product.getId(), warehouse.getId())
                    .stream()
                    .map(StockBalance::getBatch)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        final Location finalLocation = location;
        final Batch finalBatch = resolvedBatch;

        StockBalance balance = (finalBatch != null
                ? stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchId(
                        product.getId(),
                        warehouse.getId(),
                        finalLocation != null ? finalLocation.getId() : null,
                        finalBatch.getId()
                  ).or(() -> stockBalanceRepository.findByProductIdAndWarehouseIdOrderByQuantityDesc(product.getId(), warehouse.getId())
                        .stream()
                        .filter(sb -> sb.getBatch() != null && sb.getBatch().getId().equals(finalBatch.getId()))
                        .findFirst())
                : stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchIsNull(
                        product.getId(),
                        warehouse.getId(),
                        finalLocation != null ? finalLocation.getId() : null
                  ).filter(sb -> sb.getQuantity() != null && sb.getQuantity().compareTo(issueQty) >= 0))
                .or(() -> stockBalanceRepository.findAvailableBatchesForIssue(product.getId(), warehouse.getId())
                        .stream()
                        .findFirst())
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
                    sb.setLocation(finalLocation);
                    sb.setBatch(finalBatch);
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
        ledger.setLocation(balance.getLocation() != null ? balance.getLocation() : finalLocation);
        ledger.setBatch(balance.getBatch() != null ? balance.getBatch() : finalBatch);
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