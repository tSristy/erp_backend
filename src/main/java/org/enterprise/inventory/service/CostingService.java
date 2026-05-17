package org.enterprise.inventory.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.InventoryCostLayer;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.StockBalance;
import org.enterprise.inventory.enums.CostingMethod;
import org.enterprise.inventory.repository.InventoryCostLayerRepository;
import org.enterprise.inventory.repository.StockBalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CostingService {

    private final InventoryCostLayerRepository costLayerRepository;
    private final StockBalanceRepository stockBalanceRepository;

    @Transactional
    public void addCostLayer(Product product, org.enterprise.inventory.entity.Warehouse warehouse, String docType, Long docId, BigDecimal qty, BigDecimal unitCost) {
        // Create cost layer for FIFO/LIFO
        if (product.getCostingMethod() == CostingMethod.FIFO || product.getCostingMethod() == CostingMethod.LIFO) {
            InventoryCostLayer layer = new InventoryCostLayer();
            layer.setProduct(product);
            layer.setWarehouse(warehouse);
            layer.setDocumentType(docType);
            layer.setDocumentId(docId);
            layer.setReceiptDate(LocalDateTime.now());
            layer.setOriginalQty(qty);
            layer.setRemainingQty(qty);
            layer.setUnitCost(unitCost);
            costLayerRepository.save(layer);
        }
    }

    /**
     * Consumes quantity from inventory layers and returns the total Cost of Goods Sold (COGS).
     */
    @Transactional
    public BigDecimal consumeCost(Product product, org.enterprise.inventory.entity.Warehouse warehouse, BigDecimal qtyToConsume) {
        CostingMethod method = product.getCostingMethod();

        if (method == CostingMethod.AVERAGE) {
            StockBalance stock = stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchId(product.getId(), warehouse.getId(), null,null)
                    .orElseThrow(() -> new RuntimeException("Stock not found for AVG costing"));
            return stock.getAverageCost().multiply(qtyToConsume);
        }

        List<InventoryCostLayer> layers;
        if (method == CostingMethod.FIFO) {
            layers = costLayerRepository.findFifoLayers(product.getId(), warehouse.getId());
        } else {
            layers = costLayerRepository.findLifoLayers(product.getId(), warehouse.getId());
        }

        BigDecimal remainingToConsume = qtyToConsume;
        BigDecimal totalCogs = BigDecimal.ZERO;

        for (InventoryCostLayer layer : layers) {
            if (remainingToConsume.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal qtyFromLayer = layer.getRemainingQty().min(remainingToConsume);
            BigDecimal costFromLayer = qtyFromLayer.multiply(layer.getUnitCost());

            totalCogs = totalCogs.add(costFromLayer);
            remainingToConsume = remainingToConsume.subtract(qtyFromLayer);

            layer.setRemainingQty(layer.getRemainingQty().subtract(qtyFromLayer));
            costLayerRepository.save(layer);
        }

        if (remainingToConsume.compareTo(BigDecimal.ZERO) > 0) {
            // Edge case: if physical stock exists but layers don't (e.g. system was switched from AVG to FIFO mid-way)
            // Fallback to average cost for the remaining
            StockBalance stock = stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchId(product.getId(), warehouse.getId(), null,null)
                    .orElseThrow(() -> new RuntimeException("Stock not found for fallback costing"));
            
            totalCogs = totalCogs.add(remainingToConsume.multiply(stock.getAverageCost()));
        }

        return totalCogs;
    }
}
