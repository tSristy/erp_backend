package org.enterprise.pos.retail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.enterprise.pos.retail.entity.RetailTransaction;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetailTransactionService {

    private final ApplicationEventPublisher eventPublisher;
    private final org.enterprise.pos.retail.repository.RetailTransactionRepository retailTransactionRepository;

    @Transactional
    public void completeTransaction(RetailTransaction transaction) {
        log.info("Completing retail transaction: {}", transaction.getTransactionNo());
        
        // Update transaction status
        transaction.setStatus(RetailTransaction.TransactionStatus.COMPLETED);
        
        // Save transaction to DB
        transaction = retailTransactionRepository.save(transaction);
        
        Long customerId = transaction.getCustomer() != null ? transaction.getCustomer().getId() : null;
        Long warehouseId = transaction.getWarehouse() != null ? transaction.getWarehouse().getId() : null;

        List<PosTransactionCompletedEvent.LineItemDto> lineItems = transaction.getDetails().stream()
                .map(detail -> PosTransactionCompletedEvent.LineItemDto.builder()
                        .productId(detail.getProduct().getId())
                        .quantity(detail.getQuantity())
                        .unitPrice(detail.getUnitPrice())
                        .lineTotal(detail.getLineTotal())
                        .discounts(detail.getDiscounts().stream()
                                .map(d -> PosTransactionCompletedEvent.DiscountDto.builder()
                                        .discountName(d.getDiscountName())
                                        .discountAmount(d.getDiscountAmount())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        List<PosTransactionCompletedEvent.PaymentDto> payments = transaction.getPayments().stream()
                .map(payment -> PosTransactionCompletedEvent.PaymentDto.builder()
                        .paymentMode(payment.getPaymentMode().name())
                        .amount(payment.getAmount())
                        .referenceNumber(payment.getReferenceNumber())
                        .build())
                .collect(Collectors.toList());

        // Publish event for other modules (Sales, Finance, Inventory) to react
        PosTransactionCompletedEvent event = new PosTransactionCompletedEvent(
                this,
                transaction.getTransactionNo(),
                transaction.getTotalAmount(),
                transaction.getTransactionDate(),
                customerId,
                warehouseId,
                lineItems,
                payments
        );
        
        eventPublisher.publishEvent(event);
        log.info("Published PosTransactionCompletedEvent for transaction: {}", transaction.getTransactionNo());
    }
}
