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
        Long companyId = TenantContext.getCompanyId();
        invoice.setCompanyId(companyId);
        
        if (invoice.getDetails() != null) {
            invoice.getDetails().forEach(detail -> {
                detail.setPurchaseInvoice(invoice);
                detail.setCompanyId(companyId);
            });
        }
        return purchaseInvoiceRepository.save(invoice);
    }

    @Transactional
    public PurchaseInvoice updateInvoiceStatus(Long id, PurchaseInvoice.InvoiceStatus status) {
        PurchaseInvoice invoice = getInvoiceById(id);
        
        if (invoice.getStatus() != PurchaseInvoice.InvoiceStatus.DRAFT && status == PurchaseInvoice.InvoiceStatus.POSTED) {
            throw new RuntimeException("Only DRAFT invoices can be posted");
        }

        invoice.setStatus(status);
        if (status == PurchaseInvoice.InvoiceStatus.POSTED) {
            createAccountingEntry(invoice);
        }
        return purchaseInvoiceRepository.save(invoice);
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
}
