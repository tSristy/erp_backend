package org.enterprise.finance.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.finance.entity.PaymentVoucher;
import org.enterprise.finance.repository.PaymentVoucherRepository;
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

import org.enterprise.inventory.repository.PurchaseInvoiceRepository;
import org.enterprise.inventory.entity.PurchaseInvoice;
import org.enterprise.finance.entity.PaymentVoucherDetail;

@Service
@RequiredArgsConstructor
public class PaymentVoucherService {

    private final PaymentVoucherRepository paymentVoucherRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final JournalEntryService journalEntryService;

    public List<PaymentVoucher> getAllVouchers() {
        Long companyId = TenantContext.getCompanyId();
        return paymentVoucherRepository.findByCompanyId(companyId);
    }

    public PaymentVoucher getVoucherById(Long id) {
        return paymentVoucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment Voucher not found"));
    }

    @Transactional
    public PaymentVoucher createVoucher(PaymentVoucher voucher) {
        Long companyId = TenantContext.getCompanyId();
        voucher.setCompanyId(companyId);
        if (voucher.getDetails() != null) {
            for (var detail : voucher.getDetails()) {
                detail.setVoucher(voucher);
            }
        }
        return paymentVoucherRepository.save(voucher);
    }

    @Transactional
    public PaymentVoucher updateStatus(Long id, PaymentVoucher.PaymentStatus status) {
        PaymentVoucher voucher = getVoucherById(id);
        
        if (voucher.getStatus() != PaymentVoucher.PaymentStatus.DRAFT && status == PaymentVoucher.PaymentStatus.POSTED) {
            throw new RuntimeException("Only DRAFT vouchers can be posted");
        }

        voucher.setStatus(status);
        if (status == PaymentVoucher.PaymentStatus.POSTED) {
            allocateToInvoices(voucher);
            createAccountingEntry(voucher);
        }
        return paymentVoucherRepository.save(voucher);
    }

    private void allocateToInvoices(PaymentVoucher voucher) {
        BigDecimal remainingAmount = voucher.getTotalAmount();
        if (remainingAmount == null || remainingAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        if (voucher.getDetails() == null || voucher.getDetails().isEmpty()) {
            // System FIFO logic
            if (voucher.getVendor() == null) throw new RuntimeException("Vendor is required for FIFO allocation");
            
            List<PurchaseInvoice> unpaidInvoices = purchaseInvoiceRepository.findUnpaidByVendorOrderByDueDateAsc(
                    voucher.getVendor().getId(), PurchaseInvoice.InvoiceStatus.POSTED);
            
            List<PaymentVoucherDetail> generatedDetails = new ArrayList<>();
            for (PurchaseInvoice invoice : unpaidInvoices) {
                if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) break;

                BigDecimal invoiceUnpaid = invoice.getTotalAmount().subtract(invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO);
                BigDecimal allocation = remainingAmount.min(invoiceUnpaid);
                
                PaymentVoucherDetail detail = new PaymentVoucherDetail();
                detail.setVoucher(voucher);
                detail.setPurchaseInvoice(invoice);
                detail.setAllocatedAmount(allocation);
                generatedDetails.add(detail);
                
                invoice.setPaidAmount((invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO).add(allocation));
                if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) >= 0) {
                    invoice.setStatus(PurchaseInvoice.InvoiceStatus.PAID);
                }
                purchaseInvoiceRepository.save(invoice);
                
                remainingAmount = remainingAmount.subtract(allocation);
            }
            voucher.setDetails(generatedDetails);
        } else {
            // User allocated logic
            for (PaymentVoucherDetail detail : voucher.getDetails()) {
                PurchaseInvoice invoice = purchaseInvoiceRepository.findById(detail.getPurchaseInvoice().getId())
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));
                
                BigDecimal allocation = detail.getAllocatedAmount();
                invoice.setPaidAmount((invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO).add(allocation));
                if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) >= 0) {
                    invoice.setStatus(PurchaseInvoice.InvoiceStatus.PAID);
                }
                purchaseInvoiceRepository.save(invoice);
            }
        }
    }

    private void createAccountingEntry(PaymentVoucher voucher) {
        BigDecimal totalAmount = voucher.getTotalAmount();
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        if (voucher.getVendor() == null || voucher.getVendor().getVendorDetail() == null || voucher.getVendor().getVendorDetail().getAccountsPayableAccount() == null) {
            throw new RuntimeException("Accounts Payable account missing on Vendor");
        }
        
        if (voucher.getBankAccount() == null || voucher.getBankAccount().getAccount() == null) {
            throw new RuntimeException("Bank account / GL account missing on Voucher");
        }

        JournalEntry journal = new JournalEntry();
        journal.setPostingDate(LocalDate.now());
        journal.setReferenceType("PAYMENT_VOUCHER");
        journal.setReferenceId(voucher.getId());
        journal.setStatus(JournalStatus.POSTED);

        List<JournalEntryLine> lines = new ArrayList<>();

        JournalEntryLine debitLine = new JournalEntryLine();
        debitLine.setJournalEntry(journal);
        debitLine.setAccount(voucher.getVendor().getVendorDetail().getAccountsPayableAccount());
        debitLine.setDebit(totalAmount);
        debitLine.setCredit(BigDecimal.ZERO);
        debitLine.setBusinessPartner(voucher.getVendor());
        lines.add(debitLine);

        JournalEntryLine creditLine = new JournalEntryLine();
        creditLine.setJournalEntry(journal);
        creditLine.setAccount(voucher.getBankAccount().getAccount());
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(totalAmount);
        lines.add(creditLine);

        journal.setLines(lines);
        journalEntryService.save(journal);
    }
}
