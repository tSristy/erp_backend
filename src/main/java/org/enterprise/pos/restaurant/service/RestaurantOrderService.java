package org.enterprise.pos.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.event.PosTransactionCompletedEvent;
import org.enterprise.pos.restaurant.entity.KitchenOrderTicket;
import org.enterprise.pos.restaurant.entity.RestaurantOrder;
import org.enterprise.pos.restaurant.entity.RestaurantOrderDetail;
import org.enterprise.pos.restaurant.repository.KitchenOrderTicketRepository;
import org.enterprise.pos.restaurant.repository.RestaurantOrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantOrderService {

    private final RestaurantOrderRepository orderRepository;
    private final KitchenOrderTicketRepository kotRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<RestaurantOrder> findAllOrders() {
        return orderRepository.findAll();
    }

    public List<RestaurantOrder> findOrdersByType(RestaurantOrder.TransactionType type) {
        return orderRepository.findByType(type);
    }

    public Optional<RestaurantOrder> findOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional
    public RestaurantOrder createOrder(RestaurantOrder order) {
        if (order.getTableNumber() != null && !order.getTableNumber().isEmpty()) {
            Optional<RestaurantOrder> existing = orderRepository.findByTableNumberAndStatusIn(
                    order.getTableNumber(),
                    Arrays.asList(RestaurantOrder.RestaurantOrderStatus.NEW, RestaurantOrder.RestaurantOrderStatus.KOT_SENT, RestaurantOrder.RestaurantOrderStatus.SERVED)
            );
            if (existing.isPresent()) {
                throw new IllegalStateException("Table " + order.getTableNumber() + " is currently occupied.");
            }
        }
        
        // Ensure total is calculated correctly
        recalculateTotal(order);
        
        order.setStatus(RestaurantOrder.RestaurantOrderStatus.NEW);
        return orderRepository.save(order);
    }

    @Transactional
    public KitchenOrderTicket sendToKitchen(Long orderId, List<Long> detailIds) {
        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        KitchenOrderTicket kot = new KitchenOrderTicket();
        kot.setOrder(order);
        kot.setSentTime(LocalDateTime.now());
        kot.setStatus(KitchenOrderTicket.KotStatus.PENDING);
        // Generate a simple KOT number
        kot.setKotNumber("KOT-" + System.currentTimeMillis());

        for (RestaurantOrderDetail detail : order.getDetails()) {
            if (detailIds.contains(detail.getId()) && detail.getStatus() == RestaurantOrderDetail.DetailStatus.PENDING) {
                detail.setStatus(RestaurantOrderDetail.DetailStatus.SENT_TO_KITCHEN);
                detail.setKot(kot);
                kot.getDetails().add(detail);
            }
        }

        order.setStatus(RestaurantOrder.RestaurantOrderStatus.KOT_SENT);
        order.addKot(kot);
        
        orderRepository.save(order);
        return kotRepository.save(kot);
    }

    @Transactional
    public RestaurantOrder settlePayment(Long orderId) {
        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        log.info("Settling payment for Restaurant Order: {} (Type: {})", order.getOrderNo(), order.getType());
        
        if (order.getType() == RestaurantOrder.TransactionType.RETURN) {
            log.info("Bypassing KOT state machine for Return transaction.");
        }
        
        order.setStatus(RestaurantOrder.RestaurantOrderStatus.PAID);
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
        java.math.BigDecimal total = order.getDetails().stream()
                .map(RestaurantOrderDetail::getLineTotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        order.setTotalAmount(total);
    }
}
