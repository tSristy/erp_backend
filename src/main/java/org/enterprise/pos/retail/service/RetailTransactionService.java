package org.enterprise.pos.retail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.enterprise.common.util.TenantContext;
import org.enterprise.pos.retail.entity.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        if (transaction.getId() != null) {
            return updateExistingTransaction(transaction, companyId);
        }

        return createNewTransaction(transaction, companyId);
    }

    private RetailTransaction createNewTransaction(RetailTransaction transaction, Long companyId) {
        transaction.setCompanyId(companyId);

        if (transaction.getCustomer() != null && transaction.getCustomer().getId() != null) {
            transaction.setCustomer(entityManager.getReference(org.enterprise.inventory.entity.BusinessPartner.class, transaction.getCustomer().getId()));
        } else {
            transaction.setCustomer(null);
        }
        if (transaction.getWarehouse() != null && transaction.getWarehouse().getId() != null) {
            transaction.setWarehouse(entityManager.getReference(org.enterprise.inventory.entity.Warehouse.class, transaction.getWarehouse().getId()));
        } else {
            transaction.setWarehouse(null);
        }
        if (transaction.getReferenceTransaction() != null && transaction.getReferenceTransaction().getId() != null) {
            transaction.setReferenceTransaction(entityManager.getReference(RetailTransaction.class, transaction.getReferenceTransaction().getId()));
        } else {
            transaction.setReferenceTransaction(null);
        }

        if (transaction.getDetails() != null) {
            for (var detail : transaction.getDetails()) {
                detail.setTransaction(transaction);
                if (detail.getCompanyId() == null) {
                    detail.setCompanyId(companyId);
                }
                if (detail.getProduct() != null && detail.getProduct().getId() != null) {
                    detail.setProduct(entityManager.getReference(org.enterprise.inventory.entity.Product.class, detail.getProduct().getId()));
                } else {
                    detail.setProduct(null);
                }
                if (detail.getVariant() != null && detail.getVariant().getId() != null) {
                    detail.setVariant(entityManager.getReference(org.enterprise.inventory.entity.ProductVariant.class, detail.getVariant().getId()));
                } else {
                    detail.setVariant(null);
                }

                if ((detail.getSerialNo() == null || detail.getSerialNo().trim().isEmpty()) && detail.getSerialNumbers() != null && !detail.getSerialNumbers().isEmpty()) {
                    detail.setSerialNo(detail.getSerialNumbers().get(0));
                }

                if (detail.getDiscounts() != null) {
                    for (var discount : detail.getDiscounts()) {
                        discount.setTransactionDetail(detail);
                        if (discount.getCompanyId() == null) {
                            discount.setCompanyId(companyId);
                        }
                    }
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

    private RetailTransaction updateExistingTransaction(RetailTransaction incoming, Long companyId) {
        RetailTransaction existing = retailTransactionRepository.findById(incoming.getId())
                .orElseThrow(() -> new RuntimeException("Retail transaction not found with id: " + incoming.getId()));

        existing.setTransactionDate(incoming.getTransactionDate() != null ? incoming.getTransactionDate() : existing.getTransactionDate());
        if (incoming.getTransactionNo() != null && !incoming.getTransactionNo().trim().isEmpty()) {
            existing.setTransactionNo(incoming.getTransactionNo());
        }
        if (incoming.getType() != null) {
            existing.setType(incoming.getType());
        }
        if (incoming.getStatus() != null) {
            existing.setStatus(incoming.getStatus());
        }

        if (incoming.getCustomer() != null && incoming.getCustomer().getId() != null) {
            existing.setCustomer(entityManager.getReference(org.enterprise.inventory.entity.BusinessPartner.class, incoming.getCustomer().getId()));
        } else {
            existing.setCustomer(null);
        }

        existing.setWalkInCustomerName(incoming.getWalkInCustomerName());
        existing.setWalkInCustomerPhone(incoming.getWalkInCustomerPhone());

        if (incoming.getWarehouse() != null && incoming.getWarehouse().getId() != null) {
            existing.setWarehouse(entityManager.getReference(org.enterprise.inventory.entity.Warehouse.class, incoming.getWarehouse().getId()));
        }

        if (incoming.getReferenceTransaction() != null && incoming.getReferenceTransaction().getId() != null) {
            existing.setReferenceTransaction(entityManager.getReference(RetailTransaction.class, incoming.getReferenceTransaction().getId()));
        } else {
            existing.setReferenceTransaction(null);
        }

        existing.setSubTotal(incoming.getSubTotal());
        existing.setDiscountAmount(incoming.getDiscountAmount());
        existing.setTaxTotal(incoming.getTaxTotal());
        existing.setTotalAmount(incoming.getTotalAmount());
        existing.setAmountPaid(incoming.getAmountPaid());
        existing.setAmountDue(incoming.getAmountDue());

        // Update details
        updateDetails(existing, incoming.getDetails(), companyId);

        // Update payments
        updatePayments(existing, incoming.getPayments(), companyId);

        return retailTransactionRepository.save(existing);
    }

    private void updateDetails(RetailTransaction existing, List<RetailTransactionDetail> incomingDetails, Long companyId) {
        if (incomingDetails == null) {
            existing.getDetails().clear();
            return;
        }

        Map<Long, RetailTransactionDetail> existingMap = existing.getDetails().stream()
                .filter(d -> d.getId() != null)
                .collect(Collectors.toMap(RetailTransactionDetail::getId, d -> d));

        List<RetailTransactionDetail> updatedList = new ArrayList<>();

        for (RetailTransactionDetail incomingDetail : incomingDetails) {
            RetailTransactionDetail target;
            if (incomingDetail.getId() != null && existingMap.containsKey(incomingDetail.getId())) {
                target = existingMap.get(incomingDetail.getId());
            } else {
                target = new RetailTransactionDetail();
                target.setTransaction(existing);
                target.setCompanyId(companyId);
                existing.getDetails().add(target);
            }

            if (incomingDetail.getProduct() != null && incomingDetail.getProduct().getId() != null) {
                target.setProduct(entityManager.getReference(org.enterprise.inventory.entity.Product.class, incomingDetail.getProduct().getId()));
            } else {
                target.setProduct(null);
            }

            if (incomingDetail.getVariant() != null && incomingDetail.getVariant().getId() != null) {
                target.setVariant(entityManager.getReference(org.enterprise.inventory.entity.ProductVariant.class, incomingDetail.getVariant().getId()));
            } else {
                target.setVariant(null);
            }

            target.setQuantity(incomingDetail.getQuantity());
            target.setUnitPrice(incomingDetail.getUnitPrice());
            target.setDiscountAmount(incomingDetail.getDiscountAmount());
            target.setLineTotal(incomingDetail.getLineTotal());

            target.setSerialNo(incomingDetail.getSerialNo());
            target.getSerialNumbers().clear();
            if (incomingDetail.getSerialNumbers() != null) {
                for (String s : incomingDetail.getSerialNumbers()) {
                    if (s != null && !s.trim().isEmpty()) {
                        target.getSerialNumbers().add(s.trim());
                    }
                }
            }
            if ((target.getSerialNo() == null || target.getSerialNo().trim().isEmpty()) && !target.getSerialNumbers().isEmpty()) {
                target.setSerialNo(target.getSerialNumbers().get(0));
            }

            updateDiscounts(target, incomingDetail.getDiscounts(), companyId);

            updatedList.add(target);
        }

        existing.getDetails().removeIf(d -> !updatedList.contains(d));
    }

    private void updateDiscounts(RetailTransactionDetail detail, List<RetailTransactionDetailDiscount> incomingDiscounts, Long companyId) {
        if (incomingDiscounts == null) {
            detail.getDiscounts().clear();
            return;
        }

        Map<Long, RetailTransactionDetailDiscount> existingDiscMap = detail.getDiscounts().stream()
                .filter(d -> d.getId() != null)
                .collect(Collectors.toMap(RetailTransactionDetailDiscount::getId, d -> d));

        List<RetailTransactionDetailDiscount> updatedDiscounts = new ArrayList<>();

        for (RetailTransactionDetailDiscount incomingDisc : incomingDiscounts) {
            RetailTransactionDetailDiscount discTarget;
            if (incomingDisc.getId() != null && existingDiscMap.containsKey(incomingDisc.getId())) {
                discTarget = existingDiscMap.get(incomingDisc.getId());
            } else {
                discTarget = new RetailTransactionDetailDiscount();
                discTarget.setTransactionDetail(detail);
                discTarget.setCompanyId(companyId);
                detail.getDiscounts().add(discTarget);
            }

            discTarget.setDiscountName(incomingDisc.getDiscountName());
            discTarget.setDiscountAmount(incomingDisc.getDiscountAmount());
            updatedDiscounts.add(discTarget);
        }

        detail.getDiscounts().removeIf(d -> !updatedDiscounts.contains(d));
    }

    private void updatePayments(RetailTransaction existing, List<RetailTransactionPayment> incomingPayments, Long companyId) {
        if (incomingPayments == null) {
            existing.getPayments().clear();
            return;
        }

        Map<Long, RetailTransactionPayment> existingPaymentMap = existing.getPayments().stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(RetailTransactionPayment::getId, p -> p));

        List<RetailTransactionPayment> updatedPayments = new ArrayList<>();

        for (RetailTransactionPayment incomingPayment : incomingPayments) {
            RetailTransactionPayment paymentTarget;
            if (incomingPayment.getId() != null && existingPaymentMap.containsKey(incomingPayment.getId())) {
                paymentTarget = existingPaymentMap.get(incomingPayment.getId());
            } else {
                paymentTarget = new RetailTransactionPayment();
                paymentTarget.setTransaction(existing);
                paymentTarget.setCompanyId(companyId);
                existing.getPayments().add(paymentTarget);
            }

            paymentTarget.setPaymentMode(incomingPayment.getPaymentMode());
            paymentTarget.setAmount(incomingPayment.getAmount());
            paymentTarget.setReferenceNumber(incomingPayment.getReferenceNumber());
            updatedPayments.add(paymentTarget);
        }

        existing.getPayments().removeIf(p -> !updatedPayments.contains(p));
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

    public org.springframework.data.domain.Page<RetailTransaction> searchRetailTransactions(Long companyId, String searchTerm, org.springframework.data.domain.Pageable pageable) {
        return retailTransactionRepository.findAll(pageable);
    }

}