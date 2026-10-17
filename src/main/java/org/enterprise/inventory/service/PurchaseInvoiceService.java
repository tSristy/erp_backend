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

        if (invoice.getGoodsReceipt() != null) {
            boolean alreadyInvoiced = purchaseInvoiceRepository.existsByGoodsReceiptIdAndStatusNot(
                    invoice.getGoodsReceipt().getId(), PurchaseInvoice.InvoiceStatus.CANCELLED);
            if (alreadyInvoiced) {
                throw new RuntimeException("This Goods Receipt has already been invoiced.");
            }
        }

        Long companyId = TenantContext.getCompanyId();
        invoice.setCompanyId(companyId);
        
        if (invoice.getDetails() != null) {
            invoice.getDetails().forEach(detail -> {
                detail.setId(null);
                detail.setPurchaseInvoice(invoice);
                detail.setCompanyId(companyId);
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

        Long companyId = TenantContext.getCompanyId();
        if (incoming.getInvoiceDate() != null) existing.setInvoiceDate(incoming.getInvoiceDate());
        if (incoming.getDueDate() != null) existing.setDueDate(incoming.getDueDate());
        if (incoming.getVendor() != null) existing.setVendor(incoming.getVendor());
        if (incoming.getGoodsReceipt() != null) existing.setGoodsReceipt(incoming.getGoodsReceipt());
        if (incoming.getInvoiceType() != null) existing.setInvoiceType(incoming.getInvoiceType());
        if (incoming.getTotalAmount() != null) existing.setTotalAmount(incoming.getTotalAmount());

        if (incoming.getDetails() != null) {
            existing.getDetails().clear();
            incoming.getDetails().forEach(detail -> {
                detail.setId(null);
                detail.setPurchaseInvoice(existing);
                detail.setCompanyId(companyId);
                existing.getDetails().add(detail);
            });
        }
        return purchaseInvoiceRepository.save(existing);
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
        if (invoice.getGoodsReceipt() == null || invoice.getGoodsReceipt().getId() == null || invoice.getDetails() == null) {
            return;
        }
        goodsReceiptRepository.findById(invoice.getGoodsReceipt().getId()).ifPresent(grn -> {
            for (var invDetail : invoice.getDetails()) {
                if (invDetail.getProduct() == null || invDetail.getQuantity() == null) continue;
                grn.getDetails().stream()
                        .filter(gd -> gd.getProduct() != null && gd.getProduct().getId().equals(invDetail.getProduct().getId()))
                        .findFirst()
                        .ifPresent(gd -> {
                            BigDecimal current = gd.getInvoicedQuantity() != null ? gd.getInvoicedQuantity() : BigDecimal.ZERO;
                            BigDecimal updated = increment ? current.add(invDetail.getQuantity()) : current.subtract(invDetail.getQuantity()).max(BigDecimal.ZERO);
                            gd.setInvoicedQuantity(updated);
                        });
            }
            goodsReceiptRepository.save(grn);
        });
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
