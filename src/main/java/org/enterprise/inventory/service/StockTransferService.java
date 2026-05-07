package org.enterprise.inventory.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.service.JournalService;
import org.enterprise.inventory.entity.*;
import org.enterprise.inventory.enums.InventoryTransactionType;
import org.enterprise.inventory.repository.InventoryLedgerRepository;
import org.enterprise.inventory.repository.StockBalanceRepository;
import org.enterprise.inventory.repository.StockTransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final JournalService journalService;
    private final CostingService costingService;

    @Transactional
    public StockTransfer save(StockTransfer stockTransfer) {
        return stockTransferRepository.save(stockTransfer);
    }

    @Transactional
    public StockTransfer completeTransfer(Long transferId) {
        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Stock Transfer not found"));

        if (transfer.getStatus() != StockTransfer.TransferStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT transfers can be completed");
        }

        boolean isInterWarehouse = !transfer.getSourceWarehouse().getId().equals(transfer.getDestinationWarehouse().getId());
        BigDecimal totalTransferValue = BigDecimal.ZERO;

        for (StockTransferDetail detail : transfer.getDetails()) {
            Product product = detail.getProduct();
            BigDecimal transferQty = detail.getQuantity();

            // 1. Deduct from Source
            StockBalance sourceStock = stockBalanceRepository
                    .findByProductIdAndWarehouseIdAndLocationId(product.getId(), transfer.getSourceWarehouse().getId(), detail.getSourceLocation() != null ? detail.getSourceLocation().getId() : null)
                    .orElseThrow(() -> new RuntimeException("Insufficient stock at source for product " + product.getName()));

            if (sourceStock.getQuantity().compareTo(transferQty) < 0) {
                throw new RuntimeException("Insufficient stock at source for product " + product.getName() +
                        ". Required: " + transferQty + ", Available: " + sourceStock.getQuantity());
            }

            BigDecimal unitCost;
            BigDecimal transferValue;

            if (isInterWarehouse) {
                // Moving between warehouses means moving across cost layers
                transferValue = costingService.consumeCost(product, transfer.getSourceWarehouse(), transferQty);
                unitCost = transferValue.divide(transferQty, 6, RoundingMode.HALF_UP);
            } else {
                // Moving within the same warehouse just shifts average cost (no cost layer consumption)
                unitCost = Optional.ofNullable(sourceStock.getAverageCost()).orElse(BigDecimal.ZERO);
                transferValue = unitCost.multiply(transferQty);
            }

            detail.setUnitCost(unitCost);
            detail.setLineTotal(transferValue);

            sourceStock.setQuantity(sourceStock.getQuantity().subtract(transferQty));
            sourceStock.setTotalValue(sourceStock.getTotalValue().subtract(transferValue));
            if (sourceStock.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
                sourceStock.setAverageCost(BigDecimal.ZERO);
                sourceStock.setTotalValue(BigDecimal.ZERO);
            } else {
                sourceStock.setAverageCost(sourceStock.getTotalValue().divide(sourceStock.getQuantity(), 6, RoundingMode.HALF_UP));
            }
            stockBalanceRepository.save(sourceStock);

            // Source Ledger
            createLedger(transfer, detail, transfer.getSourceWarehouse(), InventoryTransactionType.TRANSFER_OUT, BigDecimal.ZERO, transferQty, unitCost, transferValue, sourceStock.getQuantity(), sourceStock.getTotalValue());

            // 2. Add to Destination
            StockBalance destStock = stockBalanceRepository
                    .findByProductIdAndWarehouseIdAndLocationId(product.getId(), transfer.getDestinationWarehouse().getId(), detail.getDestinationLocation() != null ? detail.getDestinationLocation().getId() : null)
                    .orElseGet(StockBalance::new);

            destStock.setProduct(product);
            destStock.setWarehouse(transfer.getDestinationWarehouse());
            destStock.setLocation(detail.getDestinationLocation());

            BigDecimal currentDestQty = Optional.ofNullable(destStock.getQuantity()).orElse(BigDecimal.ZERO);
            BigDecimal currentDestValue = Optional.ofNullable(destStock.getTotalValue()).orElse(BigDecimal.ZERO);

            BigDecimal newDestQty = currentDestQty.add(transferQty);
            BigDecimal newDestValue = currentDestValue.add(transferValue);

            destStock.setQuantity(newDestQty);
            destStock.setTotalValue(newDestValue);
            destStock.setAverageCost(newDestValue.divide(newDestQty, 6, RoundingMode.HALF_UP));

            stockBalanceRepository.save(destStock);

            if (isInterWarehouse) {
                // Deposit the exact consumed value as a new cost layer in the destination warehouse
                costingService.addCostLayer(product, transfer.getDestinationWarehouse(), "TRANSFER_IN", transfer.getId(), transferQty, unitCost);
            }

            // Destination Ledger
            createLedger(transfer, detail, transfer.getDestinationWarehouse(), InventoryTransactionType.TRANSFER_IN, transferQty, BigDecimal.ZERO, unitCost, transferValue, newDestQty, newDestValue);

            totalTransferValue = totalTransferValue.add(transferValue);
        }

        if (isInterWarehouse) {
            createAccountingEntry(transfer, totalTransferValue);
        }

        transfer.setStatus(StockTransfer.TransferStatus.COMPLETED);
        return stockTransferRepository.save(transfer);
    }

    private void createLedger(StockTransfer transfer, StockTransferDetail detail, Warehouse warehouse, InventoryTransactionType type, BigDecimal qtyIn, BigDecimal qtyOut, BigDecimal unitCost, BigDecimal totalCost, BigDecimal balanceQty, BigDecimal balanceCost) {
        InventoryLedger ledger = new InventoryLedger();
        ledger.setTransactionType(type);
        ledger.setDocumentType("STOCK_TRANSFER");
        ledger.setDocumentId(transfer.getId());
        ledger.setTransactionDate(LocalDateTime.now());
        ledger.setWarehouse(warehouse);
        ledger.setProduct(detail.getProduct());
        ledger.setQtyIn(qtyIn);
        ledger.setQtyOut(qtyOut);
        ledger.setUnitCost(unitCost);
        ledger.setTotalCost(totalCost);
        ledger.setBalanceQty(balanceQty);
        ledger.setBalanceCost(balanceCost);
        inventoryLedgerRepository.save(ledger);
    }

    private void createAccountingEntry(StockTransfer transfer, BigDecimal totalTransferValue) {
        if (totalTransferValue.compareTo(BigDecimal.ZERO) <= 0) return;

        Warehouse source = transfer.getSourceWarehouse();
        Warehouse dest = transfer.getDestinationWarehouse();

        if (source.getInventoryAccount() == null || dest.getInventoryAccount() == null) {
            throw new RuntimeException("Inventory account missing on source or destination warehouse");
        }

        // If both warehouses hit the same inventory account, no journal entry is needed
        if (source.getInventoryAccount().getId().equals(dest.getInventoryAccount().getId())) {
            return;
        }

        JournalEntry journal = new JournalEntry();
        journal.setPostingDate(LocalDate.now());
        journal.setReferenceType("STOCK_TRANSFER");
        journal.setReferenceId(transfer.getId());
        journal.setStatus(JournalStatus.POSTED);

        List<JournalEntryLine> lines = new ArrayList<>();

        // Dr Destination Inventory
        JournalEntryLine debitLine = new JournalEntryLine();
        debitLine.setJournalEntry(journal);
        debitLine.setAccount(dest.getInventoryAccount());
        debitLine.setDebit(totalTransferValue);
        debitLine.setCredit(BigDecimal.ZERO);
        lines.add(debitLine);

        // Cr Source Inventory
        JournalEntryLine creditLine = new JournalEntryLine();
        creditLine.setJournalEntry(journal);
        creditLine.setAccount(source.getInventoryAccount());
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(totalTransferValue);
        lines.add(creditLine);

        journal.setLines(lines);
        journalService.save(journal);
    }
}
