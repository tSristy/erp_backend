package org.enterprise.finance.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.finance.entity.PaymentReceipt;
import org.enterprise.finance.repository.PaymentReceiptRepository;
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

import org.enterprise.sales.repository.SalesInvoiceRepository;
import org.enterprise.sales.entity.SalesInvoice;
import org.enterprise.finance.entity.PaymentReceiptDetail;

@Service
@RequiredArgsConstructor
public class PaymentReceiptService {

    private final PaymentReceiptRepository paymentReceiptRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final JournalEntryService journalEntryService;

    public List<PaymentReceipt> getAllReceipts() {
        Long companyId = TenantContext.getCompanyId();
        return paymentReceiptRepository.findByCompanyId(companyId);
    }

    public PaymentReceipt getReceiptById(Long id) {
        return paymentReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment Receipt not found"));
    }

    @Transactional
    public PaymentReceipt createReceipt(PaymentReceipt receipt) {
        Long companyId = TenantContext.getCompanyId();
        receipt.setCompanyId(companyId);
        if (receipt.getDetails() != null) {
            for (var detail : receipt.getDetails()) {
                detail.setReceipt(receipt);
            }
        }
        return paymentReceiptRepository.save(receipt);
    }

    @Transactional
    public PaymentReceipt updateStatus(Long id, PaymentReceipt.PaymentStatus status) {
        PaymentReceipt receipt = getReceiptById(id);
        
        if (receipt.getStatus() != PaymentReceipt.PaymentStatus.DRAFT && status == PaymentReceipt.PaymentStatus.POSTED) {
            throw new RuntimeException("Only DRAFT receipts can be posted");
        }

        receipt.setStatus(status);
        if (status == PaymentReceipt.PaymentStatus.POSTED) {
            allocateToInvoices(receipt);
            createAccountingEntry(receipt);
        }
        return paymentReceiptRepository.save(receipt);
    }

    private void allocateToInvoices(PaymentReceipt receipt) {
        BigDecimal remainingAmount = receipt.getTotalAmount();
        if (remainingAmount == null || remainingAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        if (receipt.getDetails() == null || receipt.getDetails().isEmpty()) {
            // System FIFO logic
            if (receipt.getCustomer() == null) throw new RuntimeException("Customer is required for FIFO allocation");
            
            List<SalesInvoice> unpaidInvoices = salesInvoiceRepository.findUnpaidByCustomerOrderByDueDateAsc(
                    receipt.getCustomer().getId(), SalesInvoice.InvoiceStatus.POSTED);
            
            List<PaymentReceiptDetail> generatedDetails = new ArrayList<>();
            for (SalesInvoice invoice : unpaidInvoices) {
                if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) break;

                BigDecimal invoiceUnpaid = invoice.getTotalAmount().subtract(invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO);
                BigDecimal allocation = remainingAmount.min(invoiceUnpaid);
                
                PaymentReceiptDetail detail = new PaymentReceiptDetail();
                detail.setReceipt(receipt);
                detail.setSalesInvoice(invoice);
                detail.setAllocatedAmount(allocation);
                generatedDetails.add(detail);
                
                invoice.setPaidAmount((invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO).add(allocation));
                if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) >= 0) {
                    invoice.setStatus(SalesInvoice.InvoiceStatus.PAID);
                }
                salesInvoiceRepository.save(invoice);
                
                remainingAmount = remainingAmount.subtract(allocation);
            }
            receipt.setDetails(generatedDetails);
        } else {
            // User allocated logic
            for (PaymentReceiptDetail detail : receipt.getDetails()) {
                SalesInvoice invoice = salesInvoiceRepository.findById(detail.getSalesInvoice().getId())
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));
                
                BigDecimal allocation = detail.getAllocatedAmount();
                invoice.setPaidAmount((invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO).add(allocation));
                if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) >= 0) {
                    invoice.setStatus(SalesInvoice.InvoiceStatus.PAID);
                }
                salesInvoiceRepository.save(invoice);
            }
        }
    }

    private void createAccountingEntry(PaymentReceipt receipt) {
        BigDecimal totalAmount = receipt.getTotalAmount();
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        if (receipt.getBankAccount() == null || receipt.getBankAccount().getAccount() == null) {
            throw new RuntimeException("Bank account / GL account missing on Receipt");
        }
        
        if (receipt.getCustomer() == null || receipt.getCustomer().getCustomerDetail() == null || receipt.getCustomer().getCustomerDetail().getAccountsReceivableAccount() == null) {
            throw new RuntimeException("Accounts Receivable account missing on Customer");
        }

        JournalEntry journal = new JournalEntry();
        journal.setPostingDate(LocalDate.now());
        journal.setReferenceType("PAYMENT_RECEIPT");
        journal.setReferenceId(receipt.getId());
        journal.setStatus(JournalStatus.POSTED);

        List<JournalEntryLine> lines = new ArrayList<>();

        JournalEntryLine debitLine = new JournalEntryLine();
        debitLine.setJournalEntry(journal);
        debitLine.setAccount(receipt.getBankAccount().getAccount());
        debitLine.setDebit(totalAmount);
        debitLine.setCredit(BigDecimal.ZERO);
        lines.add(debitLine);

        JournalEntryLine creditLine = new JournalEntryLine();
        creditLine.setJournalEntry(journal);
        creditLine.setAccount(receipt.getCustomer().getCustomerDetail().getAccountsReceivableAccount());
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(totalAmount);
        creditLine.setBusinessPartner(receipt.getCustomer());
        lines.add(creditLine);

        journal.setLines(lines);
        journalEntryService.save(journal);
    }
}
