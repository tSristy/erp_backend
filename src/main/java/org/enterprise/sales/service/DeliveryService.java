package org.enterprise.sales.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.service.JournalEntryService;
import org.enterprise.inventory.entity.InventoryLedger;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.StockBalance;
import org.enterprise.inventory.entity.Warehouse;
import org.enterprise.inventory.enums.InventoryTransactionType;
import org.enterprise.inventory.repository.InventoryLedgerRepository;
import org.enterprise.inventory.repository.StockBalanceRepository;
import org.enterprise.inventory.service.CostingService;
import org.enterprise.sales.entity.DeliveryNote;
import org.enterprise.sales.entity.DeliveryNoteDetail;
import org.enterprise.sales.repository.DeliveryNoteRepository;
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
public class DeliveryService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final JournalEntryService journalService;
    private final CostingService costingService;
    private final org.enterprise.inventory.service.BatchSerialTrackingService batchSerialTrackingService;

    @Transactional
    public DeliveryNote save(DeliveryNote deliveryNote) {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new RuntimeException("No active company context");
        }

        deliveryNote.setCompanyId(companyId);
        
        if (deliveryNote.getDetails() != null) {
            for (var detail : deliveryNote.getDetails()) {
                detail.setDeliveryNote(deliveryNote);
                if (detail.getCompanyId() == null) {
                    detail.setCompanyId(companyId);
                }
            }
        }
        
        return deliveryNoteRepository.save(deliveryNote);
    }

    @Transactional
    public DeliveryNote confirmDelivery(Long deliveryNoteId) {
        DeliveryNote delivery = deliveryNoteRepository.findById(deliveryNoteId)
                .orElseThrow(() -> new RuntimeException("Delivery Note not found"));

        if (delivery.getStatus() != DeliveryNote.DeliveryStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT deliveries can be confirmed");
        }

        BigDecimal totalCogs = BigDecimal.ZERO;
        boolean isOutbound = delivery.getDeliveryType() == DeliveryNote.DeliveryType.OUTBOUND;

        List<DeliveryNoteDetail> processedDetails = new ArrayList<>();
        for (DeliveryNoteDetail detail : delivery.getDetails()) {
            if (isOutbound && detail.getProduct().getIsBatchManaged() != null && detail.getProduct().getIsBatchManaged() && detail.getBatch() == null) {
                BigDecimal remainingQty = detail.getQuantity();
                List<StockBalance> availableBatches = stockBalanceRepository.findAvailableBatchesForIssue(detail.getProduct().getId(), delivery.getWarehouse().getId());
                for (StockBalance sb : availableBatches) {
                    if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) break;
                    BigDecimal qtyToTake = sb.getQuantity().min(remainingQty);

                    DeliveryNoteDetail newDetail = new DeliveryNoteDetail();
                    newDetail.setDeliveryNote(delivery);
                    newDetail.setSalesOrderDetail(detail.getSalesOrderDetail());
                    newDetail.setProduct(detail.getProduct());
                    newDetail.setQuantity(qtyToTake);
                    newDetail.setBatch(sb.getBatch());
                    // Serial numbers are ignored for auto-split since they need manual tracking
                    processedDetails.add(newDetail);

                    remainingQty = remainingQty.subtract(qtyToTake);
                }
                if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
                    throw new RuntimeException("Insufficient batch stock for auto-allocation for product " + detail.getProduct().getName());
                }
            } else {
                processedDetails.add(detail);
            }
        }
        
        delivery.getDetails().clear();
        delivery.getDetails().addAll(processedDetails);

        for (DeliveryNoteDetail detail : delivery.getDetails()) {
            Product product = detail.getProduct();
            Warehouse warehouse = delivery.getWarehouse();
            BigDecimal issueQty = detail.getQuantity();

            StockBalance stock;
            if (detail.getBatch() != null) {
                stock = stockBalanceRepository
                        .findByProductIdAndWarehouseIdAndLocationIdAndBatchId(product.getId(), warehouse.getId(), null, detail.getBatch().getId())
                        .orElseGet(() -> isOutbound ? null : new StockBalance());
            } else {
                stock = stockBalanceRepository
                        .findByProductIdAndWarehouseIdAndLocationIdAndBatchIsNull(product.getId(), warehouse.getId(), null)
                        .orElseGet(() -> isOutbound ? null : new StockBalance());
            }
            
            if (stock == null && isOutbound) {
                throw new RuntimeException("Insufficient stock for product " + product.getName());
            }

            if (!isOutbound) {
                if (stock.getProduct() == null) {
                    stock.setProduct(product);
                    stock.setWarehouse(warehouse);
                    stock.setBatch(detail.getBatch());
                }
            }

            BigDecimal currentQty = stock.getQuantity() == null ? BigDecimal.ZERO : stock.getQuantity();
            
            if (isOutbound && currentQty.compareTo(issueQty) < 0) {
                throw new RuntimeException("Insufficient stock for product " + product.getName() +
                        ". Required: " + issueQty + ", Available: " + currentQty);
            }

            batchSerialTrackingService.validateSerialNumbers(product, detail.getSerialNumbers(), issueQty.intValue());

            BigDecimal unitCost;
            BigDecimal issueValue;
            BigDecimal newQty;
            BigDecimal newTotalValue;
            BigDecimal currentTotalValue = Optional.ofNullable(stock.getTotalValue()).orElse(BigDecimal.ZERO);

            if (isOutbound) {
                // Consume Cost Layers
                issueValue = costingService.consumeCost(product, warehouse, issueQty);
                unitCost = issueValue.divide(issueQty, 6, RoundingMode.HALF_UP);
                newQty = currentQty.subtract(issueQty);
                newTotalValue = currentTotalValue.subtract(issueValue);
            } else {
                // Inbound Return: Add Cost Layer
                unitCost = detail.getUnitCost() != null && detail.getUnitCost().compareTo(BigDecimal.ZERO) > 0 ? detail.getUnitCost() : 
                           (stock.getAverageCost() != null ? stock.getAverageCost() : BigDecimal.ZERO);
                
                issueValue = unitCost.multiply(issueQty);
                costingService.addCostLayer(product, warehouse, "DELIVERY_NOTE", delivery.getId(), issueQty, unitCost);
                newQty = currentQty.add(issueQty);
                newTotalValue = currentTotalValue.add(issueValue);
            }

            detail.setUnitCost(unitCost);

            stock.setQuantity(newQty);
            stock.setTotalValue(newTotalValue);

            if (newQty.compareTo(BigDecimal.ZERO) == 0) {
                stock.setAverageCost(BigDecimal.ZERO);
                stock.setTotalValue(BigDecimal.ZERO);
            } else if (!isOutbound && newQty.compareTo(BigDecimal.ZERO) > 0) {
                stock.setAverageCost(newTotalValue.divide(newQty, 6, RoundingMode.HALF_UP));
            }

            stockBalanceRepository.save(stock);

            if (isOutbound) {
                batchSerialTrackingService.processOutboundSerials(product, detail.getBatch(), detail.getSerialNumbers(), warehouse, null, org.enterprise.inventory.entity.SerialNumber.SerialStatus.ISSUED, InventoryTransactionType.SALES, "DELIVERY_NOTE", delivery.getId());
            } else {
                batchSerialTrackingService.processInboundSerials(product, detail.getBatch(), detail.getSerialNumbers(), warehouse, null, InventoryTransactionType.SALES_RETURN, "DELIVERY_NOTE", delivery.getId());
            }

            // Create Ledger Entry
            InventoryLedger ledger = new InventoryLedger();
            ledger.setTransactionType(isOutbound ? InventoryTransactionType.SALES : InventoryTransactionType.SALES_RETURN);
            ledger.setDocumentType("DELIVERY_NOTE");
            ledger.setDocumentId(delivery.getId());
            ledger.setTransactionDate(LocalDateTime.now());
            ledger.setWarehouse(warehouse);
            ledger.setProduct(product);
            ledger.setBatch(detail.getBatch());
            ledger.setQtyIn(isOutbound ? BigDecimal.ZERO : issueQty);
            ledger.setQtyOut(isOutbound ? issueQty : BigDecimal.ZERO);
            ledger.setUnitCost(unitCost);
            ledger.setTotalCost(issueValue);
            ledger.setBalanceQty(newQty);
            ledger.setBalanceCost(newTotalValue);

            inventoryLedgerRepository.save(ledger);
            totalCogs = totalCogs.add(issueValue);
            
            // Update Sales Order shipped/returned quantity
            if (detail.getSalesOrderDetail() != null) {
                if (isOutbound) {
                    BigDecimal currentShipped = detail.getSalesOrderDetail().getShippedQuantity() != null ? detail.getSalesOrderDetail().getShippedQuantity() : BigDecimal.ZERO;
                    detail.getSalesOrderDetail().setShippedQuantity(currentShipped.add(issueQty));
                } else {
                    BigDecimal currentReturned = detail.getSalesOrderDetail().getReturnedQuantity() != null ? detail.getSalesOrderDetail().getReturnedQuantity() : BigDecimal.ZERO;
                    detail.getSalesOrderDetail().setReturnedQuantity(currentReturned.add(issueQty));
                }
            }
        }

        createAccountingEntry(delivery, totalCogs, isOutbound);

        delivery.setStatus(DeliveryNote.DeliveryStatus.SHIPPED);
        return deliveryNoteRepository.save(delivery);
    }

    private void createAccountingEntry(DeliveryNote delivery, BigDecimal totalCogs, boolean isOutbound) {
        if (totalCogs.compareTo(BigDecimal.ZERO) <= 0) return;

        if (delivery.getWarehouse().getCogsAccount() == null || delivery.getWarehouse().getInventoryAccount() == null) {
            throw new RuntimeException("COGS or Inventory account missing on Warehouse");
        }

        JournalEntry journal = new JournalEntry();
        journal.setPostingDate(LocalDate.now());
        journal.setReferenceType("DELIVERY_NOTE");
        journal.setReferenceId(delivery.getId());
        journal.setStatus(JournalStatus.POSTED);

        List<JournalEntryLine> lines = new ArrayList<>();

        JournalEntryLine debitLine = new JournalEntryLine();
        debitLine.setJournalEntry(journal);
        debitLine.setAccount(isOutbound ? delivery.getWarehouse().getCogsAccount() : delivery.getWarehouse().getInventoryAccount());
        debitLine.setDebit(totalCogs);
        debitLine.setCredit(BigDecimal.ZERO);
        lines.add(debitLine);

        JournalEntryLine creditLine = new JournalEntryLine();
        creditLine.setJournalEntry(journal);
        creditLine.setAccount(isOutbound ? delivery.getWarehouse().getInventoryAccount() : delivery.getWarehouse().getCogsAccount());
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(totalCogs);
        lines.add(creditLine);

        journal.setLines(lines);
        journalService.save(journal);
    }

    @Transactional
    public DeliveryNote createReturn(Long originalDeliveryId) {
        DeliveryNote original = deliveryNoteRepository.findById(originalDeliveryId)
                .orElseThrow(() -> new RuntimeException("Original delivery not found"));

        DeliveryNote returnDelivery = new DeliveryNote();
        returnDelivery.setDeliveryType(DeliveryNote.DeliveryType.INBOUND_RETURN);
        returnDelivery.setSalesOrder(original.getSalesOrder());
        returnDelivery.setCustomer(original.getCustomer());
        returnDelivery.setWarehouse(original.getWarehouse());
        returnDelivery.setStatus(DeliveryNote.DeliveryStatus.DRAFT);
        returnDelivery.setDeliveryDate(LocalDate.now());

        List<DeliveryNoteDetail> returnDetails = new ArrayList<>();
        for (DeliveryNoteDetail originalDetail : original.getDetails()) {
            DeliveryNoteDetail returnDetail = new DeliveryNoteDetail();
            returnDetail.setDeliveryNote(returnDelivery);
            returnDetail.setSalesOrderDetail(originalDetail.getSalesOrderDetail());
            returnDetail.setProduct(originalDetail.getProduct());
            returnDetail.setQuantity(originalDetail.getQuantity());
            returnDetail.setUnitCost(originalDetail.getUnitCost());
            returnDetail.setBatch(originalDetail.getBatch());
            returnDetail.setSerialNumbers(originalDetail.getSerialNumbers());
            returnDetails.add(returnDetail);
        }
        returnDelivery.setDetails(returnDetails);

        return deliveryNoteRepository.save(returnDelivery);
    }

    public java.util.List<DeliveryNote> findAll() {
        return deliveryNoteRepository.findAll();
    }

    public java.util.Optional<DeliveryNote> findById(Long id) {
        return deliveryNoteRepository.findById(id);
    }

    @org.springframework.transaction.annotation.Transactional
    public void delete(Long id) {
        deliveryNoteRepository.deleteById(id);
    }
}
