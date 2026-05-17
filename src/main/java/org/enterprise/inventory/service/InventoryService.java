package org.enterprise.inventory.service;

import lombok.RequiredArgsConstructor;
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


    public List<Inventory> getByProduct(Long productId, Long companyId) {
        return inventoryRepository.findByProductIdAndCompanyId(productId, companyId);
    }

    public void adjustStock(Long inventoryId, BigDecimal qty) {
        Inventory inv = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inv.setQuantity(inv.getQuantity().add(qty));
        inventoryRepository.save(inv);
    }

    @Transactional
    public void receiveStock(InventoryTransactionRequest request) {

        Product product = productRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));

        StockBalance balance = stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchId(
                        product.getId(),
                        warehouse.getId(),
                        location.getId(),null
                )
                .orElseGet(() -> {
                    StockBalance sb = new StockBalance();
                    sb.setProduct(product);
                    sb.setWarehouse(warehouse);
                    sb.setLocation(location);
                    sb.setQuantity(BigDecimal.ZERO);
                    sb.setAverageCost(BigDecimal.ZERO);
                    sb.setTotalValue(BigDecimal.ZERO);
                    return sb;
                });

        BigDecimal oldQty = balance.getQuantity();
        BigDecimal oldValue = balance.getTotalValue();

        BigDecimal receivedQty = request.getQuantity();
        BigDecimal receivedValue = receivedQty.multiply(request.getUnitCost());

        BigDecimal newQty = oldQty.add(receivedQty);
        BigDecimal newValue = oldValue.add(receivedValue);

        BigDecimal avgCost = BigDecimal.ZERO;

        if (newQty.compareTo(BigDecimal.ZERO) > 0) {
            avgCost = newValue.divide(newQty, 4, RoundingMode.HALF_UP);
        }

        balance.setQuantity(newQty);
        balance.setAverageCost(avgCost);
        balance.setTotalValue(newValue);

        stockBalanceRepository.save(balance);

        InventoryLedger ledger = new InventoryLedger();

        ledger.setProduct(product);
        ledger.setWarehouse(warehouse);
        ledger.setLocation(location);

        ledger.setTransactionType(InventoryTransactionType.GRN);

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

    @Transactional(readOnly = true)
    public StockBalanceResponse getStockBalance(
            Long itemId,
            Long warehouseId,
            Long locationId
    ) {

        StockBalance balance = stockBalanceRepository.findByProductIdAndWarehouseIdAndLocationIdAndBatchId(
                        itemId,
                        warehouseId,
                        locationId,null
                )
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        return new StockBalanceResponse(
                balance.getProduct().getId(),
                balance.getWarehouse().getId(),
                balance.getLocation().getId(),
                balance.getQuantity(),
                balance.getAverageCost(),
                balance.getTotalValue()
        );
    }

}