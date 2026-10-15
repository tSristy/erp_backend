package org.enterprise.pos.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.enterprise.pos.restaurant.entity.KitchenOrderTicket;
import org.enterprise.pos.restaurant.entity.RestaurantOrder;
import org.enterprise.pos.restaurant.entity.RestaurantOrderDetail;
import org.enterprise.pos.restaurant.repository.KitchenOrderTicketRepository;
import org.enterprise.pos.restaurant.repository.RestaurantOrderRepository;
import org.enterprise.pos.restaurant.repository.RestaurantPaymentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.enterprise.pos.restaurant.entity.RestaurantPayment;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantOrderService {

    private final RestaurantOrderRepository orderRepository;
    private final KitchenOrderTicketRepository kotRepository;
    private final RestaurantPaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<RestaurantOrder> findAllOrders() {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        return orderRepository.findActiveOrders(companyId, null);
    }

    @Transactional(readOnly = true)
    public List<RestaurantOrder> findOrdersByType(RestaurantOrder.TransactionType type) {
        return orderRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<RestaurantOrder> findOrdersByWarehouseId(Long warehouseId) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        return orderRepository.findActiveOrders(companyId, warehouseId);
    }

    @Transactional(readOnly = true)
    public Optional<RestaurantOrder> findOrderById(Long orderId) {
        return orderRepository.findByIdWithDetails(orderId);
    }

    @Transactional
    public RestaurantOrder createOrder(RestaurantOrder order) {
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo("ORD-" + System.currentTimeMillis());
        }
        
        if (order.getWarehouse() == null && order.getWarehouseId() != null) {
            org.enterprise.inventory.entity.Warehouse wh = new org.enterprise.inventory.entity.Warehouse();
            wh.setId(order.getWarehouseId());
            order.setWarehouse(wh);
        }
        
        if (order.getCustomerId() != null) {
            org.enterprise.inventory.entity.BusinessPartner cust = new org.enterprise.inventory.entity.BusinessPartner();
            cust.setId(order.getCustomerId());
            order.setCustomer(cust);
        } else if (order.getCustomer() != null && order.getCustomer().getId() != null) {
            order.setCustomerId(order.getCustomer().getId());
        } else {
            order.setCustomer(null);
            order.setCustomerId(null);
        }

        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (order.getCompanyId() == null) {
            order.setCompanyId(companyId != null ? companyId : 1L);
        }

        // If updating an existing order (e.g. from Manage Orders edit page)
        if (order.getId() != null) {
            Optional<RestaurantOrder> existingOpt = orderRepository.findById(order.getId());
            if (existingOpt.isPresent()) {
                RestaurantOrder existing = existingOpt.get();
                existing.setCustomer(order.getCustomer());
                existing.setCustomerId(order.getCustomerId());
                if (order.getWarehouse() != null) existing.setWarehouse(order.getWarehouse());
                if (order.getWarehouseId() != null) existing.setWarehouseId(order.getWarehouseId());
                if (order.getWaiterId() != null) existing.setWaiterId(order.getWaiterId());
                if (order.getTableNumber() != null) existing.setTableNumber(order.getTableNumber());
                if (order.getOrderType() != null) existing.setOrderType(order.getOrderType());
                if (order.getStatus() != null) existing.setStatus(order.getStatus());
                if (order.getEventDateTime() != null) existing.setEventDateTime(order.getEventDateTime());

                // Update details
                if (order.getDetails() != null) {
                    existing.getDetails().clear();
                    for (RestaurantOrderDetail detail : order.getDetails()) {
                        detail.setOrder(existing);
                        detail.setCompanyId(existing.getCompanyId());
                        if (detail.getProduct() == null && detail.getProductId() != null) {
                            org.enterprise.inventory.entity.Product p = new org.enterprise.inventory.entity.Product();
                            p.setId(detail.getProductId());
                            detail.setProduct(p);
                        }
                        if (detail.getQuantity() != null && detail.getUnitPrice() != null) {
                            detail.setLineTotal(detail.getQuantity().multiply(detail.getUnitPrice()));
                        }
                        if (detail.getStatus() == null) {
                            detail.setStatus(RestaurantOrderDetail.DetailStatus.PENDING);
                        }
                        existing.getDetails().add(detail);
                    }
                }

                // Update payments
                if (order.getPayments() != null && !order.getPayments().isEmpty()) {
                    for (RestaurantPayment payment : order.getPayments()) {
                        if (payment.getId() != null && !paymentRepository.existsById(payment.getId())) {
                            payment.setId(null);
                        }
                        payment.setOrder(existing);
                        payment.setCompanyId(existing.getCompanyId());
                        RestaurantPayment savedPayment = paymentRepository.save(payment);
                        if (!existing.getPayments().contains(savedPayment)) {
                            existing.getPayments().add(savedPayment);
                        }
                    }
                    BigDecimal totalPaid = existing.getPayments().stream()
                            .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (existing.getTotalAmount() != null && totalPaid.compareTo(existing.getTotalAmount()) >= 0) {
                        existing.setStatus(RestaurantOrder.RestaurantOrderStatus.PAID);
                    }
                }

                recalculateTotal(existing);
                return orderRepository.save(existing);
            }
        }

        if (order.getTableNumber() != null && !order.getTableNumber().isEmpty()) {
            Optional<RestaurantOrder> existing = orderRepository.findByTableNumberAndStatusIn(
                    order.getTableNumber(),
                    Arrays.asList(RestaurantOrder.RestaurantOrderStatus.NEW, RestaurantOrder.RestaurantOrderStatus.KOT_SENT, RestaurantOrder.RestaurantOrderStatus.SERVED)
            );
            if (existing.isPresent()) {
                RestaurantOrder activeOrder = existing.get();
                if (order.getWaiterId() != null) {
                    activeOrder.setWaiterId(order.getWaiterId());
                }
                if (order.getId() == null || !order.getId().equals(activeOrder.getId())) {
                    // Append new items to the existing active order on this table
                    if (order.getDetails() != null) {
                        for (RestaurantOrderDetail detail : order.getDetails()) {
                            detail.setOrder(activeOrder);
                            detail.setCompanyId(activeOrder.getCompanyId());
                            if (detail.getProduct() == null && detail.getProductId() != null) {
                                org.enterprise.inventory.entity.Product p = new org.enterprise.inventory.entity.Product();
                                p.setId(detail.getProductId());
                                detail.setProduct(p);
                            }
                            if (detail.getQuantity() != null && detail.getUnitPrice() != null) {
                                detail.setLineTotal(detail.getQuantity().multiply(detail.getUnitPrice()));
                            }
                            if (detail.getStatus() == null) {
                                detail.setStatus(RestaurantOrderDetail.DetailStatus.PENDING);
                            }
                            activeOrder.getDetails().add(detail);
                        }
                    }
                    recalculateTotal(activeOrder);
                    return orderRepository.save(activeOrder);
                }
            }
        }
        
        if (order.getDetails() != null) {
            for (RestaurantOrderDetail detail : order.getDetails()) {
                detail.setOrder(order);
                if (detail.getCompanyId() == null) {
                    detail.setCompanyId(order.getCompanyId());
                }
                if (detail.getProduct() == null && detail.getProductId() != null) {
                    org.enterprise.inventory.entity.Product p = new org.enterprise.inventory.entity.Product();
                    p.setId(detail.getProductId());
                    detail.setProduct(p);
                }
                if (detail.getQuantity() != null && detail.getUnitPrice() != null) {
                    detail.setLineTotal(detail.getQuantity().multiply(detail.getUnitPrice()));
                }
                if (detail.getStatus() == null) {
                    detail.setStatus(RestaurantOrderDetail.DetailStatus.PENDING);
                }
            }
        }

        if (order.getPayments() != null) {
            for (RestaurantPayment payment : order.getPayments()) {
                payment.setOrder(order);
                if (payment.getCompanyId() == null) {
                    payment.setCompanyId(order.getCompanyId());
                }
            }
        }
        
        // Ensure total is calculated correctly
        recalculateTotal(order);
        
        if (order.getStatus() == null) {
            order.setStatus(RestaurantOrder.RestaurantOrderStatus.NEW);
        }
        RestaurantOrder savedOrder = orderRepository.save(order);

        if (order.getPayments() != null && !order.getPayments().isEmpty()) {
            for (RestaurantPayment payment : order.getPayments()) {
                if (payment.getId() != null && !paymentRepository.existsById(payment.getId())) {
                    payment.setId(null);
                }
                payment.setOrder(savedOrder);
                payment.setCompanyId(savedOrder.getCompanyId());
                paymentRepository.save(payment);
            }
        }

        return savedOrder;
    }

    @Transactional
    public KitchenOrderTicket sendToKitchen(Long orderId, List<Long> detailIds) {
        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        KitchenOrderTicket kot = new KitchenOrderTicket();
        kot.setSentTime(LocalDateTime.now());
        kot.setStatus(KitchenOrderTicket.KotStatus.PENDING);
        kot.setKotNumber("KOT-" + System.currentTimeMillis());
        kot.setCompanyId(order.getCompanyId());

        order.addKot(kot);

        for (RestaurantOrderDetail detail : order.getDetails()) {
            if (detailIds.contains(detail.getId()) && detail.getStatus() == RestaurantOrderDetail.DetailStatus.PENDING) {
                detail.setStatus(RestaurantOrderDetail.DetailStatus.SENT_TO_KITCHEN);
                detail.setKot(kot);
                kot.getDetails().add(detail);
            }
        }

        if (kot.getDetails().isEmpty()) {
            for (RestaurantOrderDetail detail : order.getDetails()) {
                if (detailIds.contains(detail.getId())) {
                    detail.setStatus(RestaurantOrderDetail.DetailStatus.SENT_TO_KITCHEN);
                    detail.setKot(kot);
                    kot.getDetails().add(detail);
                }
            }
        }

        if (kot.getDetails().isEmpty()) {
            throw new IllegalStateException("No items selected to send to kitchen");
        }

        order.setStatus(RestaurantOrder.RestaurantOrderStatus.KOT_SENT);
        kot = kotRepository.save(kot);
        orderRepository.save(order);
        return kot;
    }

    @Transactional
    public RestaurantOrder settlePayment(Long orderId) {
        return settlePayment(orderId, "CASH", null, null, null);
    }

    @Transactional
    public RestaurantOrder settlePayment(Long orderId, String paymentModeStr, BigDecimal amount, BigDecimal tipAmount, String referenceNumber) {
        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        log.info("Settling payment for Restaurant Order: {} (Type: {})", order.getOrderNo(), order.getType());
        
        if (order.getType() == RestaurantOrder.TransactionType.RETURN) {
            log.info("Bypassing KOT state machine for Return transaction.");
        }
        
        order.setStatus(RestaurantOrder.RestaurantOrderStatus.PAID);

        // Record payment
        BigDecimal payAmount = amount != null ? amount : (order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
        RestaurantPayment.PaymentMode mode = RestaurantPayment.PaymentMode.CASH;
        if (paymentModeStr != null) {
            try {
                mode = RestaurantPayment.PaymentMode.valueOf(paymentModeStr.trim().toUpperCase());
            } catch (Exception e) {
                log.warn("Invalid payment mode {}, defaulting to CASH", paymentModeStr);
            }
        }

        if (order.getPayments() == null) {
            order.setPayments(new ArrayList<>());
        }

        RestaurantPayment payment = new RestaurantPayment();
        payment.setOrder(order);
        payment.setCompanyId(order.getCompanyId());
        payment.setPaymentMode(mode);
        payment.setAmount(payAmount);
        payment.setTipAmount(tipAmount != null ? tipAmount : BigDecimal.ZERO);
        payment.setReferenceNumber(referenceNumber);

        payment = paymentRepository.save(payment);
        order.getPayments().add(payment);

        order = orderRepository.save(order);

        // Map to standard POS ERP Integration Event
        Long customerId = order.getCustomer() != null ? order.getCustomer().getId() : null;
        Long warehouseId = order.getWarehouse() != null ? order.getWarehouse().getId() : null;

        // Publish event for Sales, Finance, Inventory modules
        PosTransactionCompletedEvent event = PosTransactionCompletedEvent.fromInterfaces(
                this,
                order.getOrderNo(),
                order.getType().name(),
                order.getTotalAmount(),
                order.getOrderDate() != null ? order.getOrderDate() : LocalDateTime.now(),
                customerId,
                warehouseId,
                order.getDetails(),
                order.getPayments()
        );

        eventPublisher.publishEvent(event);
        log.info("Published PosTransactionCompletedEvent for Restaurant Order: {}", order.getOrderNo());

        return order;
    }

    @Transactional
    public RestaurantOrder splitBill(Long sourceOrderId, List<Long> detailIdsToMove) {
        RestaurantOrder sourceOrder = orderRepository.findById(sourceOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Source order not found"));

        if (sourceOrder.getStatus() == RestaurantOrder.RestaurantOrderStatus.PAID || sourceOrder.getStatus() == RestaurantOrder.RestaurantOrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot split a paid or cancelled order");
        }

        RestaurantOrder newOrder = new RestaurantOrder();
        newOrder.setOrderNo(sourceOrder.getOrderNo() + "-SPLIT");
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setTableNumber(sourceOrder.getTableNumber());
        newOrder.setWaiterId(sourceOrder.getWaiterId());
        newOrder.setOrderType(sourceOrder.getOrderType());
        newOrder.setCustomer(sourceOrder.getCustomer());
        newOrder.setWarehouse(sourceOrder.getWarehouse());
        newOrder.setStatus(sourceOrder.getStatus());
        newOrder.setType(RestaurantOrder.TransactionType.SALES);

        List<RestaurantOrderDetail> detailsToMove = sourceOrder.getDetails().stream()
                .filter(d -> detailIdsToMove.contains(d.getId()))
                .collect(Collectors.toList());

        for (RestaurantOrderDetail detail : detailsToMove) {
            sourceOrder.getDetails().remove(detail);
            newOrder.addDetail(detail);
        }

        recalculateTotal(sourceOrder);
        recalculateTotal(newOrder);

        orderRepository.save(sourceOrder);
        return orderRepository.save(newOrder);
    }

    @Transactional
    public RestaurantOrder mergeTables(Long targetOrderId, Long sourceOrderId) {
        if (targetOrderId.equals(sourceOrderId)) {
            throw new IllegalArgumentException("Cannot merge table with itself");
        }

        RestaurantOrder targetOrder = orderRepository.findById(targetOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Target order not found"));
        RestaurantOrder sourceOrder = orderRepository.findById(sourceOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Source order not found"));

        if (sourceOrder.getStatus() == RestaurantOrder.RestaurantOrderStatus.PAID || sourceOrder.getStatus() == RestaurantOrder.RestaurantOrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot merge a paid or cancelled source order");
        }

        List<RestaurantOrderDetail> allSourceDetails = List.copyOf(sourceOrder.getDetails());
        for (RestaurantOrderDetail detail : allSourceDetails) {
            sourceOrder.getDetails().remove(detail);
            targetOrder.addDetail(detail);
        }

        sourceOrder.setStatus(RestaurantOrder.RestaurantOrderStatus.CANCELLED);
        sourceOrder.setTotalAmount(java.math.BigDecimal.ZERO);
        
        recalculateTotal(targetOrder);

        orderRepository.save(sourceOrder);
        return orderRepository.save(targetOrder);
    }

    private void recalculateTotal(RestaurantOrder order) {
        if (order.getDetails() == null) {
            order.setTotalAmount(java.math.BigDecimal.ZERO);
            return;
        }
        java.math.BigDecimal total = order.getDetails().stream()
                .map(d -> d.getLineTotal() != null ? d.getLineTotal() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        order.setTotalAmount(total);
    }

    @Transactional
    public RestaurantOrder assignWaiter(Long orderId, Long waiterId) {
        RestaurantOrder order = orderRepository.findByIdWithDetails(orderId)
                .orElseGet(() -> orderRepository.findById(orderId)
                        .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId)));
        order.setWaiterId(waiterId);
        return orderRepository.save(order);
    }

    public org.springframework.data.domain.Page<RestaurantOrder> searchRestaurantOrderDtos(Long companyId, String searchTerm, org.springframework.data.domain.Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

}