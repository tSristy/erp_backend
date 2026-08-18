package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.ServicePartsRequisition;
import org.enterprise.crm.service.entity.ServicePartsRequisitionDetail;
import org.enterprise.crm.service.repository.ServicePartsRequisitionRepository;
import org.enterprise.inventory.dto.InventoryTransactionRequest;
import org.enterprise.inventory.dto.StockBalanceResponse;
import org.enterprise.inventory.service.BaseService;
import org.enterprise.inventory.service.InventoryService;
import org.enterprise.finance.service.JournalEntryService;
import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServicePartsRequisitionService extends BaseService<ServicePartsRequisition, Long> {

    private final ServicePartsRequisitionRepository repository;
    private final InventoryService inventoryService;
    private final JournalEntryService journalEntryService;

    public ServicePartsRequisitionService(ServicePartsRequisitionRepository repository,
                                          InventoryService inventoryService,
                                          JournalEntryService journalEntryService) {
        super(repository);
        this.repository = repository;
        this.inventoryService = inventoryService;
        this.journalEntryService = journalEntryService;
    }

    public List<ServicePartsRequisition> findByServiceRequestId(Long serviceRequestId) {
        return repository.findByServiceRequestId(serviceRequestId);
    }

    @Override
    @Transactional
    public ServicePartsRequisition save(ServicePartsRequisition entity) {
        ServicePartsRequisition existing = null;
        if (entity.getId() != null) {
            existing = repository.findById(entity.getId()).orElse(null);
        }

        Map<Long, BigDecimal> existingIssued = Map.of();
        if (existing != null && existing.getDetails() != null) {
            existingIssued = existing.getDetails().stream()
                    .filter(d -> d.getId() != null)
                    .collect(Collectors.toMap(
                            ServicePartsRequisitionDetail::getId,
                            d -> d.getQuantityIssued() != null ? d.getQuantityIssued() : BigDecimal.ZERO
                    ));
        }

        ServicePartsRequisition saved = super.save(entity);

        if (saved.getStatus() == ServicePartsRequisition.RequisitionStatus.ISSUED ||
            saved.getStatus() == ServicePartsRequisition.RequisitionStatus.PARTIALLY_ISSUED) {
            
            JournalEntry journalEntry = new JournalEntry();
            journalEntry.setReferenceType("SERVICE_PARTS_REQUISITION");
            journalEntry.setReferenceId(saved.getId());
            journalEntry.setStatus(JournalStatus.POSTED);
            journalEntry.setLines(new ArrayList<>());
            
            boolean hasJournalEntries = false;

            for (ServicePartsRequisitionDetail detail : saved.getDetails()) {
                BigDecimal oldIssued = existingIssued.getOrDefault(detail.getId(), BigDecimal.ZERO);
                BigDecimal newIssued = detail.getQuantityIssued() != null ? detail.getQuantityIssued() : BigDecimal.ZERO;
                BigDecimal delta = newIssued.subtract(oldIssued);

                if (delta.compareTo(BigDecimal.ZERO) > 0) {
                    if (saved.getWarehouse() == null) {
                        throw new RuntimeException("Warehouse must be selected to issue parts.");
                    }

                    // Get cost from inventory
                    StockBalanceResponse stockBalance = inventoryService.getStockBalance(
                            detail.getProduct().getId(), saved.getWarehouse().getId(), null);
                    
                    BigDecimal unitCost = stockBalance.getAverageCost() != null ? stockBalance.getAverageCost() : BigDecimal.ZERO;
                    BigDecimal totalCost = unitCost.multiply(delta);

                    // Issue stock
                    InventoryTransactionRequest req = new InventoryTransactionRequest();
                    req.setItemId(detail.getProduct().getId());
                    req.setWarehouseId(saved.getWarehouse().getId());
                    req.setTransactionType("SERVICE_ISSUE");
                    req.setDocumentType("PARTS_REQUISITION");
                    req.setDocumentId(saved.getId());
                    req.setQuantity(delta);
                    inventoryService.issueStock(req);

                    // Accounting Lines
                    if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
                        hasJournalEntries = true;

                        // Credit Inventory Asset
                        if (detail.getProduct().getInventoryAccount() != null) {
                            JournalEntryLine creditLine = new JournalEntryLine();
                            creditLine.setAccount(detail.getProduct().getInventoryAccount());
                            creditLine.setCredit(totalCost);
                            creditLine.setDebit(BigDecimal.ZERO);
                            creditLine.setJournalEntry(journalEntry);
                            journalEntry.getLines().add(creditLine);
                        } else {
                            throw new RuntimeException("Inventory Account not configured for product: " + detail.getProduct().getName());
                        }

                        // Debit COGS / Service Expense
                        if (detail.getProduct().getCogsAccount() != null) {
                            JournalEntryLine debitLine = new JournalEntryLine();
                            debitLine.setAccount(detail.getProduct().getCogsAccount());
                            debitLine.setDebit(totalCost);
                            debitLine.setCredit(BigDecimal.ZERO);
                            debitLine.setJournalEntry(journalEntry);
                            journalEntry.getLines().add(debitLine);
                        } else {
                            throw new RuntimeException("COGS/Expense Account not configured for product: " + detail.getProduct().getName());
                        }
                    }
                }
            }

            if (hasJournalEntries) {
                journalEntryService.save(journalEntry);
            }
        }

        return saved;
    }
}
