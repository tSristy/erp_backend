package org.enterprise.inventory.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.enterprise.common.util.TenantContext;
import org.enterprise.finance.entity.Account;
import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.repository.AccountRepository;
import org.enterprise.finance.service.JournalEntryService;
import org.enterprise.inventory.dto.InventoryTransactionRequest;
import org.enterprise.inventory.dto.StockBalanceResponse;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.Warehouse;
import org.enterprise.inventory.repository.ProductRepository;
import org.enterprise.inventory.repository.WarehouseRepository;
import org.enterprise.inventory.service.InventoryService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PosTransactionInventoryListener {

    private final InventoryService inventoryService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;

    /**
     * Listens for POS transactions, deducts/restores inventory, and posts the COGS vs. Inventory GL JournalEntry.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePosTransactionCompleted(PosTransactionCompletedEvent event) {
        log.info("Inventory Module received POS transaction completion event for: {} (Type: {}). Processing stock & COGS...", event.getTransactionNo(), event.getTransactionType());

        boolean isReturn = "RETURN".equalsIgnoreCase(event.getTransactionType());

        Long warehouseId = event.getWarehouseId();
        if (warehouseId == null) {
            log.warn("No warehouse specified in POS transaction {}. Skipping inventory stock & COGS adjustment.", event.getTransactionNo());
            return;
        }

        Long companyId = TenantContext.getCompanyId();
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElse(null);
        if (companyId == null && warehouse != null) {
            companyId = warehouse.getCompanyId();
        }

        Account defaultInventoryAccount = (warehouse != null && warehouse.getInventoryAccount() != null)
                ? warehouse.getInventoryAccount()
                : (companyId != null ? accountRepository.findByCodeAndCompanyId("1030", companyId) : null);
        Account defaultCogsAccount = companyId != null
                ? accountRepository.findByCodeAndCompanyId("5010", companyId)
                : null;

        List<JournalEntryLine> glLines = new ArrayList<>();
        JournalEntry cogsJournal = new JournalEntry();
        cogsJournal.setCompanyId(companyId);
        cogsJournal.setPostingDate(LocalDate.now());
        cogsJournal.setReferenceType("POS_COGS");
        cogsJournal.setStatus(JournalStatus.POSTED);

        for (PosTransactionCompletedEvent.LineItemDto item : event.getLineItems()) {
            if (item.getProductId() == null || item.getQuantity() == null) continue;
            BigDecimal qty = item.getQuantity().abs();
            if (qty.compareTo(BigDecimal.ZERO) <= 0) continue;

            try {
                Product product = productRepository.findById(item.getProductId()).orElse(null);

                InventoryTransactionRequest req = new InventoryTransactionRequest();
                req.setItemId(item.getProductId());
                req.setWarehouseId(warehouseId);
                req.setDocumentType("POS_TRANSACTION");
                req.setQuantity(qty);

                BigDecimal lineCost = BigDecimal.ZERO;

                if (isReturn) {
                    BigDecimal returnUnitCost = BigDecimal.ZERO;
                    try {
                        StockBalanceResponse bal = inventoryService.getStockBalance(item.getProductId(), warehouseId, null);
                        if (bal != null && bal.getAverageCost() != null && bal.getAverageCost().compareTo(BigDecimal.ZERO) > 0) {
                            returnUnitCost = bal.getAverageCost();
                        }
                    } catch (Exception ignored) {}
                    if (returnUnitCost.compareTo(BigDecimal.ZERO) <= 0 && item.getUnitPrice() != null) {
                        returnUnitCost = item.getUnitPrice();
                    }
                    req.setUnitCost(returnUnitCost);
                    req.setTransactionType("SALES_RETURN");
                    inventoryService.receiveStock(req);
                    lineCost = qty.multiply(returnUnitCost);
                } else {
                    req.setUnitCost(item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO);
                    req.setTransactionType("SALES");
                    lineCost = inventoryService.issueStock(req);
                }

                if (lineCost != null && lineCost.compareTo(BigDecimal.ZERO) > 0) {
                    Account itemInvAccount = (product != null && product.getInventoryAccount() != null)
                            ? product.getInventoryAccount()
                            : defaultInventoryAccount;
                    Account itemCogsAccount = (product != null && product.getCogsAccount() != null)
                            ? product.getCogsAccount()
                            : defaultCogsAccount;

                    if (itemInvAccount != null && itemCogsAccount != null) {
                        // Line 1: COGS (Debit on Sale, Credit on Return)
                        JournalEntryLine cogsLine = new JournalEntryLine();
                        cogsLine.setJournalEntry(cogsJournal);
                        cogsLine.setCompanyId(companyId);
                        cogsLine.setAccount(itemCogsAccount);
                        cogsLine.setDebit(isReturn ? BigDecimal.ZERO : lineCost);
                        cogsLine.setCredit(isReturn ? lineCost : BigDecimal.ZERO);
                        glLines.add(cogsLine);

                        // Line 2: Inventory (Credit on Sale, Debit on Return)
                        JournalEntryLine invLine = new JournalEntryLine();
                        invLine.setJournalEntry(cogsJournal);
                        invLine.setCompanyId(companyId);
                        invLine.setAccount(itemInvAccount);
                        invLine.setDebit(isReturn ? lineCost : BigDecimal.ZERO);
                        invLine.setCredit(isReturn ? BigDecimal.ZERO : lineCost);
                        glLines.add(invLine);
                    }
                }
            } catch (Exception ex) {
                log.warn("Inventory/COGS processing warning for Product ID {} in POS Transaction {}: {}",
                        item.getProductId(), event.getTransactionNo(), ex.getMessage());
            }
        }

        if (!glLines.isEmpty()) {
            try {
                cogsJournal.setLines(glLines);
                journalEntryService.save(cogsJournal);
                log.info("COGS & Inventory GL JournalEntry posted for POS Transaction: {}", event.getTransactionNo());
            } catch (Exception ex) {
                log.warn("Could not auto-post POS COGS JournalEntry for {}: {}", event.getTransactionNo(), ex.getMessage());
            }
        }

        log.info("Inventory & COGS processing finished for POS Transaction: {}", event.getTransactionNo());
    }
}
