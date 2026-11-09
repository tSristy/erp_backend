package org.enterprise.pos.retail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.enterprise.finance.listener.PosTransactionFinanceListener;
import org.enterprise.inventory.listener.PosTransactionInventoryListener;
import org.enterprise.pos.restaurant.entity.RestaurantOrder;
import org.enterprise.pos.restaurant.repository.RestaurantOrderRepository;
import org.enterprise.pos.retail.entity.RetailTransaction;
import org.enterprise.pos.retail.repository.RetailTransactionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PosTransactionReconciliationService {

    private final RetailTransactionRepository retailTransactionRepository;
    private final RestaurantOrderRepository restaurantOrderRepository;
    private final PosTransactionFinanceListener posTransactionFinanceListener;
    private final PosTransactionInventoryListener posTransactionInventoryListener;
    private final JdbcTemplate jdbcTemplate;

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onApplicationReady() {
        reconcileCompletedPosTransactions();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reconcileCompletedPosTransactions() {
        try {
            List<RetailTransaction> completedRetail = retailTransactionRepository.findAll().stream()
                    .filter(t -> t.getStatus() == RetailTransaction.TransactionStatus.COMPLETED)
                    .toList();

            for (RetailTransaction tx : completedRetail) {
                if (tx.getId() == null) continue;

                // 1. Backfill any orphan POS_TRANSACTION journal entry with null reference_id matching this transaction
                if (tx.getTotalAmount() != null) {
                    jdbcTemplate.update(
                            "UPDATE fin_journal_entries SET reference_id = ?, journal_no = COALESCE(NULLIF(journal_no, ''), ?) " +
                            "WHERE id IN (" +
                            "  SELECT id FROM fin_journal_entries " +
                            "  WHERE reference_type = 'POS_TRANSACTION' AND reference_id IS NULL " +
                            "    AND (company_id = ? OR company_id IS NULL) " +
                            "    AND ABS(COALESCE(total_debit, 0) - ?) < 0.01 " +
                            "  ORDER BY id ASC LIMIT 1" +
                            ")",
                            tx.getId(),
                            "JV-POS-" + tx.getTransactionNo(),
                            tx.getCompanyId(),
                            tx.getTotalAmount().abs()
                    );
                }

                // Fallback: if there is still any POS_TRANSACTION / POS_COGS journal entry with null reference_id for the same company
                jdbcTemplate.update(
                        "UPDATE fin_journal_entries SET reference_id = ? " +
                        "WHERE id IN (" +
                        "  SELECT id FROM fin_journal_entries " +
                        "  WHERE reference_type IN ('POS_TRANSACTION', 'POS_COGS') AND reference_id IS NULL " +
                        "    AND (company_id = ? OR company_id IS NULL) " +
                        "  ORDER BY id ASC LIMIT 1" +
                        ")",
                        tx.getId(),
                        tx.getCompanyId()
                );

                // Backfill any orphan POS_TRANSACTION inventory ledger rows with null document_id
                if (tx.getDetails() != null) {
                    boolean isReturnTx = tx.getType() == RetailTransaction.TransactionType.RETURN;
                    String targetSerialStatus = isReturnTx ? "IN_STOCK" : "ISSUED";

                    for (var detail : tx.getDetails()) {
                        if (detail.getProduct() != null && detail.getProduct().getId() != null) {
                            Long pid = detail.getProduct().getId();
                            jdbcTemplate.update(
                                    "UPDATE inv_inventory_ledger SET document_id = ? " +
                                    "WHERE id IN (" +
                                    "  SELECT id FROM inv_inventory_ledger " +
                                    "  WHERE document_type = 'POS_TRANSACTION' AND document_id IS NULL " +
                                    "    AND product_id = ? " +
                                    "  ORDER BY id ASC LIMIT 1" +
                                    ")",
                                    tx.getId(),
                                    pid
                            );

                            // Reconcile serial numbers status in inv_serial_numbers
                            java.util.LinkedHashSet<String> detailSerials = new java.util.LinkedHashSet<>();
                            if (detail.getSerialNumbers() != null) {
                                for (String sn : detail.getSerialNumbers()) {
                                    if (sn != null && !sn.trim().isEmpty()) {
                                        detailSerials.add(sn.trim());
                                    }
                                }
                            }
                            if (detail.getSerialNo() != null && !detail.getSerialNo().trim().isEmpty()) {
                                detailSerials.add(detail.getSerialNo().trim());
                            }
                            for (String sn : detailSerials) {
                                jdbcTemplate.update(
                                        "UPDATE inv_serial_numbers SET status = ? WHERE serial_no = ? AND product_id = ?",
                                        targetSerialStatus,
                                        sn,
                                        pid
                                );
                            }
                        }
                    }
                }

                Long customerId = tx.getCustomer() != null ? tx.getCustomer().getId() : null;
                Long warehouseId = tx.getWarehouse() != null ? tx.getWarehouse().getId() : null;

                PosTransactionCompletedEvent event = PosTransactionCompletedEvent.fromInterfaces(
                        this,
                        tx.getId(),
                        tx.getCompanyId(),
                        tx.getTransactionNo(),
                        tx.getType() != null ? tx.getType().name() : "SALES",
                        tx.getTotalAmount(),
                        tx.getTransactionDate() != null ? tx.getTransactionDate() : LocalDateTime.now(),
                        customerId,
                        warehouseId,
                        tx.getDetails(),
                        tx.getPayments()
                );

                // 2. Ensure Finance GL JournalEntry exists for this completed POS transaction
                Integer finCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM fin_journal_entries WHERE reference_type = 'POS_TRANSACTION' AND reference_id = ?",
                        Integer.class,
                        tx.getId()
                );
                if (finCount == null || finCount == 0) {
                    posTransactionFinanceListener.handlePosTransactionCompleted(event);
                }

                // 3. Ensure InventoryLedger movement & POS_COGS GL JournalEntry exist for this completed POS transaction
                Integer invCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM inv_inventory_ledger WHERE document_type = 'POS_TRANSACTION' AND document_id = ?",
                        Integer.class,
                        tx.getId()
                );
                if ((invCount == null || invCount == 0) && tx.getDetails() != null && !tx.getDetails().isEmpty()) {
                    posTransactionInventoryListener.handlePosTransactionCompleted(event);
                }
            }

            List<RestaurantOrder> paidOrders = restaurantOrderRepository.findByStatus(RestaurantOrder.RestaurantOrderStatus.PAID);
            for (RestaurantOrder order : paidOrders) {
                if (order.getId() == null) continue;

                Long customerId = order.getCustomer() != null ? order.getCustomer().getId() : order.getCustomerId();
                Long warehouseId = order.getWarehouse() != null ? order.getWarehouse().getId() : order.getWarehouseId();

                PosTransactionCompletedEvent event = PosTransactionCompletedEvent.fromInterfaces(
                        this,
                        order.getId(),
                        order.getCompanyId(),
                        order.getOrderNo(),
                        order.getType() != null ? order.getType().name() : "SALES",
                        order.getTotalAmount(),
                        order.getOrderDate() != null ? order.getOrderDate() : LocalDateTime.now(),
                        customerId,
                        warehouseId,
                        order.getDetails(),
                        order.getPayments()
                );

                Integer finCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM fin_journal_entries WHERE reference_type = 'POS_TRANSACTION' AND reference_id = ?",
                        Integer.class,
                        order.getId()
                );
                if (finCount == null || finCount == 0) {
                    posTransactionFinanceListener.handlePosTransactionCompleted(event);
                }

                Integer invCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM inv_inventory_ledger WHERE document_type = 'POS_TRANSACTION' AND document_id = ?",
                        Integer.class,
                        order.getId()
                );
                if ((invCount == null || invCount == 0) && order.getDetails() != null && !order.getDetails().isEmpty()) {
                    posTransactionInventoryListener.handlePosTransactionCompleted(event);
                }
            }
        } catch (Exception ex) {
            log.warn("Non-fatal warning during POS transaction reconciliation: {}", ex.getMessage());
        }
    }
}
