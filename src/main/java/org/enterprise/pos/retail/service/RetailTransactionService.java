package org.enterprise.pos.retail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.enterprise.pos.retail.entity.RetailTransaction;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Publish event for other modules (Sales, Finance, Inventory) to react
        PosTransactionCompletedEvent event = PosTransactionCompletedEvent.fromInterfaces(
                this,
                transaction.getTransactionNo(),
                transaction.getType().name(),
                transaction.getTotalAmount(),
                transaction.getTransactionDate(),
                customerId,
                warehouseId,
                transaction.getDetails(),
                transaction.getPayments()
        );
        
        eventPublisher.publishEvent(event);
        log.info("Published PosTransactionCompletedEvent for transaction: {}", transaction.getTransactionNo());
    }
}
