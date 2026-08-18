package org.enterprise.inventory.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.*;
import org.enterprise.inventory.enums.InventoryTransactionType;
import org.enterprise.inventory.repository.InventoryLedgerRepository;
import org.enterprise.inventory.repository.StockBalanceRepository;
import org.enterprise.inventory.repository.StockReclassificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StockReclassificationService extends BaseService<StockReclassification, Long> {

    private final StockReclassificationRepository stockReclassificationRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final CostingService costingService;

    public StockReclassificationService(StockReclassificationRepository stockReclassificationRepository,
                                        StockBalanceRepository stockBalanceRepository,
                                        InventoryLedgerRepository inventoryLedgerRepository,
                                        CostingService costingService) {
        super(stockReclassificationRepository);
        this.stockReclassificationRepository = stockReclassificationRepository;
        this.stockBalanceRepository = stockBalanceRepository;
        this.inventoryLedgerRepository = inventoryLedgerRepository;
        this.costingService = costingService;
    }

    @Override
    @Transactional
    public StockReclassification save(StockReclassification stockReclassification) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (stockReclassification.getCompanyId() == null) {
            stockReclassification.setCompanyId(companyId);
        }
        
        if (stockReclassification.getDetails() != null) {
            for (StockReclassificationDetail detail : stockReclassification.getDetails()) {
                if (detail.getCompanyId() == null) {
                    detail.setCompanyId(companyId);
                }
                detail.setStockReclassification(stockReclassification);
            }
        }
        return super.save(stockReclassification);
    }

    @Transactional
    public StockReclassification completeReclassification(Long reclassId) {
        StockReclassification reclass = stockReclassificationRepository.findById(reclassId)
                .orElseThrow(() -> new RuntimeException("Stock Reclassification not found"));

        if (reclass.getStatus() != StockReclassification.ReclassStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT reclassifications can be completed");
        }

        Warehouse warehouse = reclass.getWarehouse();

        java.util.List<StockReclassificationDetail> processedDetails = new java.util.ArrayList<>();
        for (StockReclassificationDetail detail : reclass.getDetails()) {
            if (detail.getSourceProduct().getIsBatchManaged() != null && detail.getSourceProduct().getIsBatchManaged() && detail.getSourceBatch() == null) {
                BigDecimal remainingQty = detail.getSourceQuantity();
                java.util.List<StockBalance> availableBatches = stockBalanceRepository.findAvailableBatchesForIssue(detail.getSourceProduct().getId(), warehouse.getId());
                for (StockBalance sb : availableBatches) {
                    if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) break;
                    BigDecimal qtyToTake = sb.getQuantity().min(remainingQty);

                    StockReclassificationDetail newDetail = new StockReclassificationDetail();
                    newDetail.setStockReclassification(reclass);
                    newDetail.setSourceProduct(detail.getSourceProduct());
                    newDetail.setDestinationProduct(detail.getDestinationProduct());
                    newDetail.setSourceQuantity(qtyToTake);
                    // Pro-rate destination quantity
                    BigDecimal destQtyToGive = qtyToTake.multiply(detail.getDestinationQuantity()).divide(detail.getSourceQuantity(), 6, java.math.RoundingMode.HALF_UP);
                    newDetail.setDestinationQuantity(destQtyToGive);
                    newDetail.setSourceBatch(sb.getBatch());
                    newDetail.setDestinationBatch(detail.getDestinationBatch()); // User can optionally specify dest batch
                    newDetail.setCompanyId(detail.getCompanyId());
                    processedDetails.add(newDetail);

                    remainingQty = remainingQty.subtract(qtyToTake);
                }
                if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
                    throw new RuntimeException("Insufficient batch stock for auto-allocation for source product " + detail.getSourceProduct().getName());
                }
            } else {
                processedDetails.add(detail);
            }
        }
        
        reclass.getDetails().clear();
        reclass.getDetails().addAll(processedDetails);

        for (StockReclassificationDetail detail : reclass.getDetails()) {
            Product sourceProduct = detail.getSourceProduct();
            Product destProduct = detail.getDestinationProduct();
            BigDecimal sourceQty = detail.getSourceQuantity();
            BigDecimal destQty = detail.getDestinationQuantity();

            // 1. Deduct Source Product
            StockBalance sourceStock;
            if (detail.getSourceBatch() != null) {
                sourceStock = stockBalanceRepository
                        .findByProductIdAndWarehouseIdAndLocationIdAndBatchId(sourceProduct.getId(), warehouse.getId(), null, detail.getSourceBatch().getId())
                        .orElseThrow(() -> new RuntimeException("Insufficient stock for source product " + sourceProduct.getName() + " and batch"));
            } else {
                sourceStock = stockBalanceRepository
                        .findByProductIdAndWarehouseIdAndLocationIdAndBatchIsNull(sourceProduct.getId(), warehouse.getId(), null)
                        .orElseThrow(() -> new RuntimeException("Insufficient stock for source product " + sourceProduct.getName()));
            }

            if (sourceStock.getQuantity().compareTo(sourceQty) < 0) {
                throw new RuntimeException("Insufficient stock for source product " + sourceProduct.getName());
            }

            // Consume Cost
            BigDecimal consumedValue = costingService.consumeCost(sourceProduct, warehouse, sourceQty);
            BigDecimal sourceUnitCost = consumedValue.divide(sourceQty, 6, RoundingMode.HALF_UP);

            sourceStock.setQuantity(sourceStock.getQuantity().subtract(sourceQty));
            sourceStock.setTotalValue(sourceStock.getTotalValue().subtract(consumedValue));
            if (sourceStock.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                sourceStock.setAverageCost(sourceStock.getTotalValue().divide(sourceStock.getQuantity(), 6, RoundingMode.HALF_UP));
            } else {
                sourceStock.setAverageCost(BigDecimal.ZERO);
                sourceStock.setTotalValue(BigDecimal.ZERO);
            }
            stockBalanceRepository.save(sourceStock);

            // Source Ledger
            createLedger(reclass, sourceProduct, warehouse, InventoryTransactionType.ADJUSTMENT_OUT, BigDecimal.ZERO, sourceQty, sourceUnitCost, consumedValue, sourceStock.getQuantity(), sourceStock.getTotalValue());

            // 2. Receive Destination Product
            BigDecimal destUnitCost = consumedValue.divide(destQty, 6, RoundingMode.HALF_UP);

            StockBalance destStock;
            if (detail.getDestinationBatch() != null) {
                destStock = stockBalanceRepository
                        .findByProductIdAndWarehouseIdAndLocationIdAndBatchId(destProduct.getId(), warehouse.getId(), null, detail.getDestinationBatch().getId())
                        .orElseGet(StockBalance::new);
            } else {
                destStock = stockBalanceRepository
                        .findByProductIdAndWarehouseIdAndLocationIdAndBatchIsNull(destProduct.getId(), warehouse.getId(), null)
                        .orElseGet(StockBalance::new);
            }

            destStock.setProduct(destProduct);
            destStock.setWarehouse(warehouse);
            destStock.setBatch(detail.getDestinationBatch());

            BigDecimal currentDestQty = Optional.ofNullable(destStock.getQuantity()).orElse(BigDecimal.ZERO);
            BigDecimal currentDestValue = Optional.ofNullable(destStock.getTotalValue()).orElse(BigDecimal.ZERO);

            BigDecimal newDestQty = currentDestQty.add(destQty);
            BigDecimal newDestValue = currentDestValue.add(consumedValue);

            destStock.setQuantity(newDestQty);
            destStock.setTotalValue(newDestValue);
            destStock.setAverageCost(newDestValue.divide(newDestQty, 6, RoundingMode.HALF_UP));

            stockBalanceRepository.save(destStock);

            // Add Cost Layer for Dest
            costingService.addCostLayer(destProduct, warehouse, "RECLASSIFICATION", reclass.getId(), destQty, destUnitCost);

            // Dest Ledger
            createLedger(reclass, destProduct, warehouse, InventoryTransactionType.ADJUSTMENT_IN, destQty, BigDecimal.ZERO, destUnitCost, consumedValue, newDestQty, newDestValue);

            detail.setUnitCost(sourceUnitCost);
            detail.setLineTotal(consumedValue);
        }

        reclass.setStatus(StockReclassification.ReclassStatus.COMPLETED);
        return stockReclassificationRepository.save(reclass);
    }

    private void createLedger(StockReclassification reclass, Product product, Warehouse warehouse, InventoryTransactionType type, BigDecimal qtyIn, BigDecimal qtyOut, BigDecimal unitCost, BigDecimal totalCost, BigDecimal balanceQty, BigDecimal balanceCost) {
        InventoryLedger ledger = new InventoryLedger();
        ledger.setTransactionType(type);
        ledger.setDocumentType("STOCK_RECLASS");
        ledger.setDocumentId(reclass.getId());
        ledger.setTransactionDate(LocalDateTime.now());
        ledger.setWarehouse(warehouse);
        ledger.setProduct(product);
        ledger.setQtyIn(qtyIn);
        ledger.setQtyOut(qtyOut);
        ledger.setUnitCost(unitCost);
        ledger.setTotalCost(totalCost);
        ledger.setBalanceQty(balanceQty);
        ledger.setBalanceCost(balanceCost);
        inventoryLedgerRepository.save(ledger);
    }
}
