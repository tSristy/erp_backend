package org.enterprise.sales.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.sales.entity.SalesInvoice;
import org.enterprise.sales.entity.SalesInvoiceDetail;
import org.enterprise.sales.service.SalesInvoiceService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class PosTransactionSalesListener {

    private final SalesInvoiceService salesInvoiceService;

    /**
     * Listens for POS transactions and generates an invoice automatically.
     * Uses TransactionalEventListener to ensure it runs after the POS transaction commits.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePosTransactionCompleted(PosTransactionCompletedEvent event) {
        log.info("Sales Module received POS transaction completion event for: {}. Generating Invoice...", event.getTransactionNo());
        
        SalesInvoice invoice = new SalesInvoice();
        invoice.setInvoiceNo("INV-" + event.getTransactionNo());
        invoice.setInvoiceDate(event.getTransactionDate().toLocalDate());
        invoice.setTotalAmount(event.getTotalAmount());
        
        if (event.getCustomerId() != null) {
            BusinessPartner customer = new BusinessPartner();
            customer.setId(event.getCustomerId());
            invoice.setCustomer(customer);
        } else {
            // Fallback for Walk-in customer
            log.info("No customer specified, generating invoice for default Walk-in Customer.");
            
            // TODO: Update this to match the ID or Code of the Walk-in BusinessPartner you create in the database
            // Example: Long walkInCustomerId = businessPartnerService.findByCode("WALKIN", companyId).getId();
            BusinessPartner walkInCustomer = new BusinessPartner();
            walkInCustomer.setId(1L); // <-- Replace 1L with your actual Walk-in Customer ID
            invoice.setCustomer(walkInCustomer);
        }

        invoice.setDetails(event.getLineItems().stream().map(dto -> {
            SalesInvoiceDetail detail = new SalesInvoiceDetail();
            detail.setQuantity(dto.getQuantity());
            detail.setUnitPrice(dto.getUnitPrice());
            detail.setLineTotal(dto.getLineTotal());
            detail.setSalesInvoice(invoice);
            return detail;
        }).collect(Collectors.toList()));

        salesInvoiceService.save(invoice);
        log.info("Sales Invoice generated successfully for POS Transaction: {}", event.getTransactionNo());
    }
}
