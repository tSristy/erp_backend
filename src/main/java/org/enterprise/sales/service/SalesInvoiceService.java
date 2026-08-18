package org.enterprise.sales.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.service.JournalEntryService;
import org.enterprise.sales.entity.SalesInvoice;
import org.enterprise.sales.entity.SalesInvoiceDetail;
import org.enterprise.sales.repository.SalesInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesInvoiceService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final JournalEntryService journalService;

    @Transactional
    public SalesInvoice save(SalesInvoice salesInvoice) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (salesInvoice.getCompanyId() == null) {
            salesInvoice.setCompanyId(companyId);
        }
        if (salesInvoice.getDetails() != null) {
            for (var detail : salesInvoice.getDetails()) {
                detail.setSalesInvoice(salesInvoice);
                if (detail.getCompanyId() == null) {
                    detail.setCompanyId(companyId);
                }
            }
        }
        return salesInvoiceRepository.save(salesInvoice);
    }

    @Transactional
    public SalesInvoice postInvoice(Long invoiceId) {
        SalesInvoice invoice = salesInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Sales Invoice not found"));

        if (invoice.getStatus() != SalesInvoice.InvoiceStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT invoices can be posted");
        }

        boolean isInvoice = invoice.getInvoiceType() == SalesInvoice.InvoiceType.INVOICE;
        createAccountingEntry(invoice, isInvoice);

        invoice.setStatus(SalesInvoice.InvoiceStatus.POSTED);
        return salesInvoiceRepository.save(invoice);
    }

    private void createAccountingEntry(SalesInvoice invoice, boolean isInvoice) {
        BigDecimal totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal discountTotal = invoice.getDiscountTotal() != null ? invoice.getDiscountTotal() : BigDecimal.ZERO;
        BigDecimal subTotal = invoice.getSubTotal() != null ? invoice.getSubTotal() : totalAmount.add(discountTotal);
        
        if (subTotal.compareTo(BigDecimal.ZERO) <= 0) return;

        if (invoice.getCustomer().getCustomerDetail() == null || invoice.getCustomer().getCustomerDetail().getAccountsReceivableAccount() == null) {
            throw new RuntimeException("Accounts Receivable account missing on Customer");
        }
        
        if (isInvoice && invoice.getWarehouse().getSalesRevenueAccount() == null) {
            throw new RuntimeException("Sales Revenue account missing on Warehouse");
        }
        
        if (!isInvoice && invoice.getWarehouse().getSalesReturnAccount() == null) {
            throw new RuntimeException("Sales Return account missing on Warehouse");
        }
        
        if (discountTotal.compareTo(BigDecimal.ZERO) > 0 && invoice.getWarehouse().getSalesDiscountAccount() == null) {
            throw new RuntimeException("Sales Discount account missing on Warehouse");
        }

        JournalEntry journal = new JournalEntry();
        journal.setPostingDate(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate() : LocalDate.now());
        journal.setReferenceType("SALES_INVOICE");
        journal.setReferenceId(invoice.getId());
        journal.setStatus(JournalStatus.POSTED);

        List<JournalEntryLine> lines = new ArrayList<>();

        if (isInvoice) {
            // Invoice: Debit AR (Net), Debit Discount, Credit Revenue (Gross)
            JournalEntryLine arLine = new JournalEntryLine();
            arLine.setJournalEntry(journal);
            arLine.setAccount(invoice.getCustomer().getCustomerDetail().getAccountsReceivableAccount());
            arLine.setBusinessPartner(invoice.getCustomer());
            arLine.setBranch(invoice.getWarehouse().getBranch());
            arLine.setDebit(totalAmount);
            arLine.setCredit(BigDecimal.ZERO);
            lines.add(arLine);
            
            if (discountTotal.compareTo(BigDecimal.ZERO) > 0) {
                JournalEntryLine discountLine = new JournalEntryLine();
                discountLine.setJournalEntry(journal);
                discountLine.setAccount(invoice.getWarehouse().getSalesDiscountAccount());
                discountLine.setBranch(invoice.getWarehouse().getBranch());
                discountLine.setDebit(discountTotal);
                discountLine.setCredit(BigDecimal.ZERO);
                lines.add(discountLine);
            }
            
            JournalEntryLine revenueLine = new JournalEntryLine();
            revenueLine.setJournalEntry(journal);
            revenueLine.setAccount(invoice.getWarehouse().getSalesRevenueAccount());
            revenueLine.setBranch(invoice.getWarehouse().getBranch());
            revenueLine.setDebit(BigDecimal.ZERO);
            revenueLine.setCredit(subTotal);
            lines.add(revenueLine);
        } else {
            // Return: Debit Return (Gross), Credit AR (Net), Credit Discount
            JournalEntryLine returnLine = new JournalEntryLine();
            returnLine.setJournalEntry(journal);
            returnLine.setAccount(invoice.getWarehouse().getSalesReturnAccount());
            returnLine.setBranch(invoice.getWarehouse().getBranch());
            returnLine.setDebit(subTotal);
            returnLine.setCredit(BigDecimal.ZERO);
            lines.add(returnLine);
            
            if (discountTotal.compareTo(BigDecimal.ZERO) > 0) {
                JournalEntryLine discountLine = new JournalEntryLine();
                discountLine.setJournalEntry(journal);
                discountLine.setAccount(invoice.getWarehouse().getSalesDiscountAccount());
                discountLine.setBranch(invoice.getWarehouse().getBranch());
                discountLine.setDebit(BigDecimal.ZERO);
                discountLine.setCredit(discountTotal);
                lines.add(discountLine);
            }
            
            JournalEntryLine arLine = new JournalEntryLine();
            arLine.setJournalEntry(journal);
            arLine.setAccount(invoice.getCustomer().getCustomerDetail().getAccountsReceivableAccount());
            arLine.setBusinessPartner(invoice.getCustomer());
            arLine.setBranch(invoice.getWarehouse().getBranch());
            arLine.setDebit(BigDecimal.ZERO);
            arLine.setCredit(totalAmount);
            lines.add(arLine);
        }

        journal.setLines(lines);
        journalService.save(journal);
    }

    @Transactional
    public SalesInvoice createCreditMemo(Long originalInvoiceId) {
        SalesInvoice original = salesInvoiceRepository.findById(originalInvoiceId)
                .orElseThrow(() -> new RuntimeException("Original invoice not found"));

        SalesInvoice creditMemo = new SalesInvoice();
        creditMemo.setInvoiceType(SalesInvoice.InvoiceType.CREDIT_MEMO);
        creditMemo.setDeliveryNote(original.getDeliveryNote());
        creditMemo.setCustomer(original.getCustomer());
        creditMemo.setWarehouse(original.getWarehouse());
        creditMemo.setStatus(SalesInvoice.InvoiceStatus.DRAFT);
        creditMemo.setInvoiceDate(LocalDate.now());

        if (original.getDiscounts() != null) {
            List<org.enterprise.sales.entity.SalesInvoiceDiscount> copiedDiscounts = new ArrayList<>();
            for (var d : original.getDiscounts()) {
                org.enterprise.sales.entity.SalesInvoiceDiscount cd = new org.enterprise.sales.entity.SalesInvoiceDiscount();
                cd.setSalesInvoice(creditMemo);
                cd.setDiscountName(d.getDiscountName());
                cd.setDiscountAmount(d.getDiscountAmount());
                copiedDiscounts.add(cd);
            }
            creditMemo.setDiscounts(copiedDiscounts);
        }

        List<SalesInvoiceDetail> creditDetails = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal subTotal = BigDecimal.ZERO;

        for (SalesInvoiceDetail originalDetail : original.getDetails()) {
            SalesInvoiceDetail creditDetail = new SalesInvoiceDetail();
            creditDetail.setSalesInvoice(creditMemo);
            creditDetail.setDeliveryNoteDetail(originalDetail.getDeliveryNoteDetail());
            creditDetail.setProduct(originalDetail.getProduct());
            creditDetail.setQuantity(originalDetail.getQuantity());
            creditDetail.setUnitPrice(originalDetail.getUnitPrice());
            creditDetail.setLineTotal(originalDetail.getLineTotal());
            creditDetail.setDiscountTotal(originalDetail.getDiscountTotal());

            if (originalDetail.getDiscounts() != null) {
                List<org.enterprise.sales.entity.SalesInvoiceDetailDiscount> copiedDetailDiscounts = new ArrayList<>();
                for (var d : originalDetail.getDiscounts()) {
                    org.enterprise.sales.entity.SalesInvoiceDetailDiscount cd = new org.enterprise.sales.entity.SalesInvoiceDetailDiscount();
                    cd.setSalesInvoiceDetail(creditDetail);
                    cd.setDiscountName(d.getDiscountName());
                    cd.setDiscountAmount(d.getDiscountAmount());
                    copiedDetailDiscounts.add(cd);
                }
                creditDetail.setDiscounts(copiedDetailDiscounts);
            }

            subTotal = subTotal.add(originalDetail.getLineTotal());
            if (originalDetail.getDiscountTotal() != null) {
                discountTotal = discountTotal.add(originalDetail.getDiscountTotal());
            }

            creditDetails.add(creditDetail);
        }

        if (original.getDiscountTotal() != null) {
            discountTotal = original.getDiscountTotal();
        }
        if (original.getSubTotal() != null) {
            subTotal = original.getSubTotal();
        }
        if (original.getTotalAmount() != null) {
            totalAmount = original.getTotalAmount();
        } else {
            totalAmount = subTotal.subtract(discountTotal);
        }

        creditMemo.setDetails(creditDetails);
        creditMemo.setSubTotal(subTotal);
        creditMemo.setDiscountTotal(discountTotal);
        creditMemo.setTotalAmount(totalAmount);

        return salesInvoiceRepository.save(creditMemo);
    }

    public java.util.List<SalesInvoice> findAll() {
        return salesInvoiceRepository.findAll();
    }

    public java.util.Optional<SalesInvoice> findById(Long id) {
        return salesInvoiceRepository.findById(id);
    }

    @org.springframework.transaction.annotation.Transactional
    public void delete(Long id) {
        salesInvoiceRepository.deleteById(id);
    }
}
