package org.enterprise.sales.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.service.JournalService;
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
    private final JournalService journalService;

    @Transactional
    public SalesInvoice save(SalesInvoice salesInvoice) {
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
        BigDecimal totalAmount = invoice.getTotalAmount();
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        if (invoice.getCustomer().getAccountsReceivableAccount() == null) {
            throw new RuntimeException("Accounts Receivable account missing on Customer");
        }
        
        if (isInvoice && invoice.getWarehouse().getSalesRevenueAccount() == null) {
            throw new RuntimeException("Sales Revenue account missing on Warehouse");
        }
        
        if (!isInvoice && invoice.getWarehouse().getSalesReturnAccount() == null) {
            throw new RuntimeException("Sales Return account missing on Warehouse");
        }

        JournalEntry journal = new JournalEntry();
        journal.setPostingDate(LocalDate.now());
        journal.setReferenceType("SALES_INVOICE");
        journal.setReferenceId(invoice.getId());
        journal.setStatus(JournalStatus.POSTED);

        List<JournalEntryLine> lines = new ArrayList<>();

        JournalEntryLine debitLine = new JournalEntryLine();
        debitLine.setJournalEntry(journal);
        debitLine.setAccount(isInvoice ? invoice.getCustomer().getAccountsReceivableAccount() : invoice.getWarehouse().getSalesReturnAccount());
        debitLine.setDebit(totalAmount);
        debitLine.setCredit(BigDecimal.ZERO);
        lines.add(debitLine);

        JournalEntryLine creditLine = new JournalEntryLine();
        creditLine.setJournalEntry(journal);
        creditLine.setAccount(isInvoice ? invoice.getWarehouse().getSalesRevenueAccount() : invoice.getCustomer().getAccountsReceivableAccount());
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(totalAmount);
        lines.add(creditLine);

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

        List<SalesInvoiceDetail> creditDetails = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesInvoiceDetail originalDetail : original.getDetails()) {
            SalesInvoiceDetail creditDetail = new SalesInvoiceDetail();
            creditDetail.setSalesInvoice(creditMemo);
            creditDetail.setDeliveryNoteDetail(originalDetail.getDeliveryNoteDetail());
            creditDetail.setProduct(originalDetail.getProduct());
            creditDetail.setQuantity(originalDetail.getQuantity());
            creditDetail.setUnitPrice(originalDetail.getUnitPrice());
            creditDetail.setLineTotal(originalDetail.getLineTotal());
            totalAmount = totalAmount.add(originalDetail.getLineTotal());
            creditDetails.add(creditDetail);
        }
        creditMemo.setDetails(creditDetails);
        creditMemo.setTotalAmount(totalAmount);

        return salesInvoiceRepository.save(creditMemo);
    }
}
