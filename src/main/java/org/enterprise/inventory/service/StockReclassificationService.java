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
@RequiredArgsConstructor
public class StockReclassificationService {

    private final StockReclassificationRepository stockReclassificationRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final CostingService costingService;

    @Transactional
    public StockReclassification save(StockReclassification stockReclassification) {
        return stockReclassificationRepository.save(stockReclassification);
    }

    @Transactional
    public StockReclassification completeReclassification(Long reclassId) {
        StockReclassification reclass = stockReclassificationRepository.findById(reclassId)
                .orElseThrow(() -> new RuntimeException("Stock Reclassification not found"));

        if (reclass.getStatus() != StockReclassification.ReclassStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT reclassifications can be completed");
        }

        Warehouse warehouse = reclass.getWarehouse();

        for (StockReclassificationDetail detail : reclass.getDetails()) {
            Product sourceProduct = detail.getSourceProduct();
            Product destProduct = detail.getDestinationProduct();
            BigDecimal sourceQty = detail.getSourceQuantity();
            BigDecimal destQty = detail.getDestinationQuantity();

            // 1. Deduct Source Product
            StockBalance sourceStock = stockBalanceRepository
                    .findByProductIdAndWarehouseIdAndLocationId(sourceProduct.getId(), warehouse.getId(), null)
                    .orElseThrow(() -> new RuntimeException("Insufficient stock for source product " + sourceProduct.getName()));

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

            StockBalance destStock = stockBalanceRepository
                    .findByProductIdAndWarehouseIdAndLocationId(destProduct.getId(), warehouse.getId(), null)
                    .orElseGet(StockBalance::new);

            destStock.setProduct(destProduct);
            destStock.setWarehouse(warehouse);

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
