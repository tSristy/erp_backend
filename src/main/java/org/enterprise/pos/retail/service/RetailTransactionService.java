package org.enterprise.pos.retail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.enterprise.common.util.TenantContext;
import org.enterprise.pos.retail.entity.RetailTransaction;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetailTransactionService {

    private final ApplicationEventPublisher eventPublisher;
    private final org.enterprise.pos.retail.repository.RetailTransactionRepository retailTransactionRepository;
    private final EntityManager entityManager;

    @Transactional
    public RetailTransaction save(RetailTransaction transaction) {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new RuntimeException("No active company context");
        }

        transaction.setCompanyId(companyId);

        if (transaction.getCustomer() != null) {
            transaction.setCustomer(transaction.getCustomer().getId() != null ? entityManager.getReference(org.enterprise.inventory.entity.BusinessPartner.class, transaction.getCustomer().getId()) : null);
        }
        if (transaction.getWarehouse() != null) {
            transaction.setWarehouse(transaction.getWarehouse().getId() != null ? entityManager.getReference(org.enterprise.inventory.entity.Warehouse.class, transaction.getWarehouse().getId()) : null);
        }
        if (transaction.getReferenceTransaction() != null) {
            transaction.setReferenceTransaction(transaction.getReferenceTransaction().getId() != null ? entityManager.getReference(RetailTransaction.class, transaction.getReferenceTransaction().getId()) : null);
        }

        if (transaction.getDetails() != null) {
            for (var detail : transaction.getDetails()) {
                detail.setTransaction(transaction);
                if (detail.getCompanyId() == null) {
                    detail.setCompanyId(companyId);
                }
                if (detail.getProduct() != null) {
                    detail.setProduct(detail.getProduct().getId() != null ? entityManager.getReference(org.enterprise.inventory.entity.Product.class, detail.getProduct().getId()) : null);
                }
            }
        }

        if (transaction.getPayments() != null) {
            for (var payment : transaction.getPayments()) {
                payment.setTransaction(transaction);
                if (payment.getCompanyId() == null) {
                    payment.setCompanyId(companyId);
                }
            }
        }

        return retailTransactionRepository.save(transaction);
    }

    @Transactional
    public void completeTransaction(RetailTransaction transaction) {
        if (transaction.getTransactionNo() == null || transaction.getTransactionNo().trim().isEmpty()) {
            transaction.setTransactionNo("RET-" + System.currentTimeMillis());
        }
        log.info("Completing retail transaction: {}", transaction.getTransactionNo());
        if (transaction.getType() == null) {
            transaction.setType(RetailTransaction.TransactionType.SALES);
        }
        transaction.setStatus(RetailTransaction.TransactionStatus.COMPLETED);
        
        transaction = this.save(transaction);
        
        Long customerId = transaction.getCustomer() != null ? transaction.getCustomer().getId() : null;
        Long warehouseId = transaction.getWarehouse() != null ? transaction.getWarehouse().getId() : null;

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

    @Transactional(readOnly = true)
    public java.util.List<RetailTransaction> findAll() {
        return retailTransactionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public java.util.Optional<RetailTransaction> findById(Long id) {
        return retailTransactionRepository.findById(id);
    }

    @Transactional
    public void delete(Long id) {
        retailTransactionRepository.deleteById(id);
    }
}
