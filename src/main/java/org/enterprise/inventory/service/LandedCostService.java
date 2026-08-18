package org.enterprise.inventory.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.inventory.entity.GoodsReceipt;
import org.enterprise.inventory.entity.InventoryCostLayer;
import org.enterprise.inventory.entity.LandedCostVoucher;
import org.enterprise.inventory.repository.InventoryCostLayerRepository;
import org.enterprise.inventory.repository.LandedCostVoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.service.JournalEntryService;
import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class LandedCostService {

    private final LandedCostVoucherRepository landedCostVoucherRepository;
    private final InventoryCostLayerRepository inventoryCostLayerRepository;
    private final JournalEntryService journalEntryService;

    public List<LandedCostVoucher> getAllVouchers() {
        Long companyId = TenantContext.getCompanyId();
        return landedCostVoucherRepository.findByCompanyId(companyId);
    }

    public LandedCostVoucher getVoucherById(Long id) {
        return landedCostVoucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Landed Cost Voucher not found"));
    }

    @Transactional
    public LandedCostVoucher createVoucher(LandedCostVoucher voucher) {
        Long companyId = TenantContext.getCompanyId();
        voucher.setCompanyId(companyId);
        
        if (voucher.getVoucherNo() == null || voucher.getVoucherNo().trim().isEmpty()) {
            java.time.LocalDate date = voucher.getPostingDate() != null ? voucher.getPostingDate() : java.time.LocalDate.now();
            int year = date.getYear();
            int month = date.getMonthValue();
            long count = landedCostVoucherRepository.countByCompanyIdAndYearAndMonth(companyId, year, month);
            voucher.setVoucherNo(String.format("LCV-%d%02d-%04d", year, month, count + 1));
        }
        
        if (voucher.getDetails() != null) {
            voucher.getDetails().forEach(detail -> detail.setVoucher(voucher));
        }
        return landedCostVoucherRepository.save(voucher);
    }

    @Transactional
    public LandedCostVoucher postVoucher(Long id) {
        LandedCostVoucher voucher = getVoucherById(id);
        if (voucher.getStatus() == LandedCostVoucher.LandedCostStatus.POSTED) {
            throw new RuntimeException("Voucher is already posted");
        }

        GoodsReceipt grn = voucher.getGoodsReceipt();
        BigDecimal totalSecondaryCost = voucher.getTotalSecondaryCost();
        BigDecimal totalGrnValue = grn.getTotalAmount();

        if (totalGrnValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("GRN total value must be greater than zero for value-based distribution.");
        }

        // Fetch the cost layers associated with this GRN
        List<InventoryCostLayer> costLayers = inventoryCostLayerRepository.findByDocumentTypeAndDocumentId("GRN", grn.getId());

        for (InventoryCostLayer layer : costLayers) {
            // Find corresponding line in GRN to get the line's original value
            grn.getDetails().stream()
                .filter(d -> d.getProduct().getId().equals(layer.getProduct().getId()))
                .findFirst()
                .ifPresent(detail -> {
                    BigDecimal lineValue = detail.getLineTotal();
                    BigDecimal valueRatio = lineValue.divide(totalGrnValue, 6, RoundingMode.HALF_UP);
                    
                    // Allocate proportion of secondary cost to this line
                    BigDecimal allocatedCost = totalSecondaryCost.multiply(valueRatio);
                    
                    // Calculate additional unit cost
                    BigDecimal additionalUnitCost = allocatedCost.divide(layer.getOriginalQty(), 6, RoundingMode.HALF_UP);
                    
                    // Update Inventory Cost Layer
                    layer.setUnitCost(layer.getUnitCost().add(additionalUnitCost));
                    inventoryCostLayerRepository.save(layer);
                });
        }

        voucher.setStatus(LandedCostVoucher.LandedCostStatus.POSTED);
        
        // Generate Journal Entry
        createAccountingEntry(voucher, grn);
        
        return landedCostVoucherRepository.save(voucher);
    }

    private void createAccountingEntry(LandedCostVoucher voucher, GoodsReceipt grn) {
        BigDecimal totalSecondaryCost = voucher.getTotalSecondaryCost();
        if (totalSecondaryCost == null || totalSecondaryCost.compareTo(BigDecimal.ZERO) <= 0) return;

        if (grn.getWarehouse() == null || grn.getWarehouse().getInventoryAccount() == null) {
            throw new RuntimeException("Inventory account missing on Warehouse for Landed Cost");
        }

        JournalEntry journal = new JournalEntry();
        journal.setPostingDate(LocalDate.now());
        journal.setReferenceType("LANDED_COST");
        journal.setReferenceId(voucher.getId());
        journal.setStatus(JournalStatus.POSTED);

        List<JournalEntryLine> lines = new ArrayList<>();

        // Debit Inventory Account for the total secondary cost
        JournalEntryLine debitLine = new JournalEntryLine();
        debitLine.setJournalEntry(journal);
        debitLine.setAccount(grn.getWarehouse().getInventoryAccount());
        debitLine.setDebit(totalSecondaryCost);
        debitLine.setCredit(BigDecimal.ZERO);
        lines.add(debitLine);

        // Credit the respective Cost Head accounts
        for (var detail : voucher.getDetails()) {
            if (detail.getAmount() == null || detail.getAmount().compareTo(BigDecimal.ZERO) <= 0) continue;
            
            if (detail.getCostHead() == null || detail.getCostHead().getAccount() == null) {
                throw new RuntimeException("GL Account missing on Cost Head: " + (detail.getCostHead() != null ? detail.getCostHead().getName() : "Unknown"));
            }

            JournalEntryLine creditLine = new JournalEntryLine();
            creditLine.setJournalEntry(journal);
            creditLine.setAccount(detail.getCostHead().getAccount());
            creditLine.setDebit(BigDecimal.ZERO);
            creditLine.setCredit(detail.getAmount());
            lines.add(creditLine);
        }

        journal.setLines(lines);
        journalEntryService.save(journal);
    }
}
