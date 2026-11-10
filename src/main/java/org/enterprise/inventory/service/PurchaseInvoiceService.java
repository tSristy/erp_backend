package org.enterprise.inventory.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.inventory.entity.PurchaseInvoice;
import org.enterprise.inventory.repository.PurchaseInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.service.JournalEntryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PurchaseInvoiceService {

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final org.enterprise.inventory.repository.GoodsReceiptRepository goodsReceiptRepository;
    private final JournalEntryService journalEntryService;

    public List<PurchaseInvoice> getAllInvoices() {
        Long companyId = TenantContext.getCompanyId();
        return purchaseInvoiceRepository.findByCompanyId(companyId);
    }

    public List<PurchaseInvoice> getUnpaidInvoicesByVendor(Long vendorId) {
        return purchaseInvoiceRepository.findUnpaidByVendorOrderByDueDateAsc(vendorId, PurchaseInvoice.InvoiceStatus.POSTED);
    }

    public PurchaseInvoice getInvoiceById(Long id) {
        return purchaseInvoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Invoice not found"));
    }

    @Transactional
    public PurchaseInvoice createInvoice(PurchaseInvoice invoice) {
        if (invoice.getInvoiceNo() == null || invoice.getInvoiceNo().trim().isEmpty()) {
            invoice.setInvoiceNo("INV-" + System.currentTimeMillis());
        }

        resolveAndValidateGoodsReceipts(invoice, null);

        Long companyId = TenantContext.getCompanyId();
        invoice.setCompanyId(companyId);
        
        if (invoice.getDetails() != null) {
            invoice.getDetails().forEach(detail -> {
                detail.setId(null);
                detail.setPurchaseInvoice(invoice);
                detail.setCompanyId(companyId);
                if (detail.getGoodsReceipt() == null && invoice.getGoodsReceipt() != null) {
                    detail.setGoodsReceipt(invoice.getGoodsReceipt());
                }
            });
        }
        return purchaseInvoiceRepository.save(invoice);
    }

    @Transactional
    public PurchaseInvoice updateInvoice(Long id, PurchaseInvoice incoming) {
        PurchaseInvoice existing = getInvoiceById(id);
        if (existing.getStatus() != PurchaseInvoice.InvoiceStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT purchase invoices can be updated");
        }

        resolveAndValidateGoodsReceipts(incoming, id);

        Long companyId = TenantContext.getCompanyId();
        if (incoming.getInvoiceDate() != null) existing.setInvoiceDate(incoming.getInvoiceDate());
        if (incoming.getDueDate() != null) existing.setDueDate(incoming.getDueDate());
        if (incoming.getVendor() != null) existing.setVendor(incoming.getVendor());
        existing.setPurchaseOrder(incoming.getPurchaseOrder());
        existing.setGoodsReceipt(incoming.getGoodsReceipt());
        existing.setGoodsReceipts(incoming.getGoodsReceipts() != null ? new ArrayList<>(incoming.getGoodsReceipts()) : new ArrayList<>());
        if (incoming.getInvoiceType() != null) existing.setInvoiceType(incoming.getInvoiceType());
        if (incoming.getTotalAmount() != null) existing.setTotalAmount(incoming.getTotalAmount());

        if (incoming.getDetails() != null) {
            existing.getDetails().clear();
            incoming.getDetails().forEach(detail -> {
                detail.setId(null);
                detail.setPurchaseInvoice(existing);
                detail.setCompanyId(companyId);
                if (detail.getGoodsReceipt() == null && existing.getGoodsReceipt() != null) {
                    detail.setGoodsReceipt(existing.getGoodsReceipt());
                }
                existing.getDetails().add(detail);
            });
        }
        return purchaseInvoiceRepository.save(existing);
    }

    private void resolveAndValidateGoodsReceipts(PurchaseInvoice invoice, Long excludeInvoiceId) {
        java.util.LinkedHashSet<Long> grnIds = new java.util.LinkedHashSet<>();

        if (invoice.getGoodsReceipts() != null) {
            for (var grn : invoice.getGoodsReceipts()) {
                if (grn != null && grn.getId() != null) {
                    grnIds.add(grn.getId());
                }
            }
        }
        if (invoice.getGoodsReceipt() != null && invoice.getGoodsReceipt().getId() != null) {
            grnIds.add(invoice.getGoodsReceipt().getId());
        }
        if (invoice.getDetails() != null) {
            for (var detail : invoice.getDetails()) {
                if (detail != null && detail.getGoodsReceipt() != null && detail.getGoodsReceipt().getId() != null) {
                    grnIds.add(detail.getGoodsReceipt().getId());
                }
            }
        }

        List<org.enterprise.inventory.entity.GoodsReceipt> resolvedGrns = new ArrayList<>();
        for (Long grnId : grnIds) {
            boolean alreadyInvoiced = purchaseInvoiceRepository.isGoodsReceiptInvoiced(
                    grnId, PurchaseInvoice.InvoiceStatus.CANCELLED, excludeInvoiceId);
            if (alreadyInvoiced) {
                throw new RuntimeException("Goods Receipt (ID: " + grnId + ") has already been invoiced.");
            }
            goodsReceiptRepository.findById(grnId).ifPresent(resolvedGrns::add);
        }

        invoice.setGoodsReceipts(resolvedGrns);
        if (!resolvedGrns.isEmpty()) {
            invoice.setGoodsReceipt(resolvedGrns.get(0));
            if (invoice.getPurchaseOrder() == null && resolvedGrns.get(0).getPurchaseOrder() != null) {
                invoice.setPurchaseOrder(resolvedGrns.get(0).getPurchaseOrder());
            }
            if (invoice.getVendor() == null && resolvedGrns.get(0).getVendor() != null) {
                invoice.setVendor(resolvedGrns.get(0).getVendor());
            }
        } else {
            invoice.setGoodsReceipt(null);
        }
    }

    @Transactional
    public void deleteInvoice(Long id) {
        PurchaseInvoice invoice = getInvoiceById(id);
        if (invoice.getStatus() != PurchaseInvoice.InvoiceStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT purchase invoices can be deleted");
        }
        purchaseInvoiceRepository.delete(invoice);
    }

    @Transactional
    public PurchaseInvoice updateInvoiceStatus(Long id, PurchaseInvoice.InvoiceStatus status) {
        PurchaseInvoice invoice = getInvoiceById(id);
        PurchaseInvoice.InvoiceStatus previousStatus = invoice.getStatus();

        if (previousStatus == PurchaseInvoice.InvoiceStatus.PAID) {
            throw new RuntimeException("Cannot change status of a PAID invoice");
        }
        if (previousStatus == PurchaseInvoice.InvoiceStatus.CANCELLED) {
            throw new RuntimeException("Cannot change status of a CANCELLED invoice");
        }
        if (previousStatus != PurchaseInvoice.InvoiceStatus.DRAFT && status == PurchaseInvoice.InvoiceStatus.POSTED) {
            throw new RuntimeException("Only DRAFT invoices can be posted");
        }

        invoice.setStatus(status);
        if (status == PurchaseInvoice.InvoiceStatus.POSTED) {
            adjustGrnInvoicedQty(invoice, true);
            createAccountingEntry(invoice);
        } else if (status == PurchaseInvoice.InvoiceStatus.CANCELLED && previousStatus == PurchaseInvoice.InvoiceStatus.POSTED) {
            adjustGrnInvoicedQty(invoice, false);
            journalEntryService.reverseByReference("PURCHASE_INVOICE", invoice.getId());
        }
        return purchaseInvoiceRepository.save(invoice);
    }

    private void adjustGrnInvoicedQty(PurchaseInvoice invoice, boolean increment) {
        if (invoice.getDetails() == null || invoice.getDetails().isEmpty()) {
            return;
        }

        java.util.LinkedHashSet<Long> grnIds = new java.util.LinkedHashSet<>();
        if (invoice.getGoodsReceipts() != null) {
            invoice.getGoodsReceipts().forEach(g -> {
                if (g != null && g.getId() != null) grnIds.add(g.getId());
            });
        }
        if (invoice.getGoodsReceipt() != null && invoice.getGoodsReceipt().getId() != null) {
            grnIds.add(invoice.getGoodsReceipt().getId());
        }
        for (var invDetail : invoice.getDetails()) {
            if (invDetail.getGoodsReceipt() != null && invDetail.getGoodsReceipt().getId() != null) {
                grnIds.add(invDetail.getGoodsReceipt().getId());
            }
        }

        if (grnIds.isEmpty()) {
            return;
        }

        java.util.Map<Long, org.enterprise.inventory.entity.GoodsReceipt> grnMap = new java.util.LinkedHashMap<>();
        for (Long grnId : grnIds) {
            goodsReceiptRepository.findById(grnId).ifPresent(grn -> grnMap.put(grnId, grn));
        }

        for (var invDetail : invoice.getDetails()) {
            if (invDetail.getProduct() == null || invDetail.getQuantity() == null) continue;

            Long targetGrnId = invDetail.getGoodsReceipt() != null ? invDetail.getGoodsReceipt().getId() : null;
            Long targetGrnDetailId = invDetail.getGoodsReceiptDetail() != null ? invDetail.getGoodsReceiptDetail().getId() : null;

            List<org.enterprise.inventory.entity.GoodsReceipt> candidateGrns = new ArrayList<>();
            if (targetGrnId != null && grnMap.containsKey(targetGrnId)) {
                candidateGrns.add(grnMap.get(targetGrnId));
            } else {
                candidateGrns.addAll(grnMap.values());
            }

            boolean matched = false;
            for (var grn : candidateGrns) {
                if (grn.getDetails() == null) continue;
                var matchOpt = grn.getDetails().stream()
                        .filter(gd -> {
                            if (targetGrnDetailId != null && targetGrnDetailId.equals(gd.getId())) {
                                return true;
                            }
                            return gd.getProduct() != null && gd.getProduct().getId().equals(invDetail.getProduct().getId());
                        })
                        .findFirst();
                if (matchOpt.isPresent()) {
                    var gd = matchOpt.get();
                    BigDecimal current = gd.getInvoicedQuantity() != null ? gd.getInvoicedQuantity() : BigDecimal.ZERO;
                    BigDecimal updated = increment ? current.add(invDetail.getQuantity()) : current.subtract(invDetail.getQuantity()).max(BigDecimal.ZERO);
                    gd.setInvoicedQuantity(updated);
                    matched = true;
                    break;
                }
            }
        }

        for (var grn : grnMap.values()) {
            goodsReceiptRepository.save(grn);
        }
    }

    private void createAccountingEntry(PurchaseInvoice invoice) {
        BigDecimal totalAmount = invoice.getTotalAmount();
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        if (invoice.getVendor() == null || invoice.getVendor().getVendorDetail() == null || invoice.getVendor().getVendorDetail().getAccountsPayableAccount() == null) {
            throw new RuntimeException("Accounts Payable account missing on Vendor");
        }
        
        if (invoice.getVendor().getVendorDetail().getGrnClearingAccount() == null) {
            throw new RuntimeException("GRN Clearing account missing on Vendor");
        }

        boolean isInvoice = invoice.getInvoiceType() == PurchaseInvoice.InvoiceType.INVOICE;

        JournalEntry journal = new JournalEntry();
        journal.setPostingDate(LocalDate.now());
        journal.setReferenceType("PURCHASE_INVOICE");
        journal.setReferenceId(invoice.getId());
        journal.setStatus(JournalStatus.POSTED);

        List<JournalEntryLine> lines = new ArrayList<>();

        JournalEntryLine debitLine = new JournalEntryLine();
        debitLine.setJournalEntry(journal);
        debitLine.setAccount(isInvoice ? invoice.getVendor().getVendorDetail().getGrnClearingAccount() : invoice.getVendor().getVendorDetail().getAccountsPayableAccount());
        debitLine.setDebit(totalAmount);
        debitLine.setCredit(BigDecimal.ZERO);
        if (!isInvoice) {
            debitLine.setBusinessPartner(invoice.getVendor());
        }
        lines.add(debitLine);

        JournalEntryLine creditLine = new JournalEntryLine();
        creditLine.setJournalEntry(journal);
        creditLine.setAccount(isInvoice ? invoice.getVendor().getVendorDetail().getAccountsPayableAccount() : invoice.getVendor().getVendorDetail().getGrnClearingAccount());
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(totalAmount);
        if (isInvoice) {
            creditLine.setBusinessPartner(invoice.getVendor());
        }
        lines.add(creditLine);

        journal.setLines(lines);
        journalEntryService.save(journal);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.PurchaseInvoice> searchPurchaseInvoices(String query, org.springframework.data.domain.Pageable pageable) {
        return purchaseInvoiceRepository.findAll(org.enterprise.inventory.specification.PurchaseInvoiceSpecification.searchByQuery(query), pageable);
    }
}
