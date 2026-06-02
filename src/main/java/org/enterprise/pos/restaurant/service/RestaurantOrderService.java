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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantOrderService {

    private final RestaurantOrderRepository orderRepository;
    private final KitchenOrderTicketRepository kotRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RestaurantOrder createOrder(RestaurantOrder order) {
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

        log.info("Settling payment for Restaurant Order: {}", order.getOrderNo());
        order.setStatus(RestaurantOrder.RestaurantOrderStatus.PAID);
        order = orderRepository.save(order);

        // Map to standard POS ERP Integration Event
        Long customerId = order.getCustomer() != null ? order.getCustomer().getId() : null;
        Long warehouseId = order.getWarehouse() != null ? order.getWarehouse().getId() : null;

        List<PosTransactionCompletedEvent.LineItemDto> lineItems = order.getDetails().stream()
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

        List<PosTransactionCompletedEvent.PaymentDto> payments = order.getPayments().stream()
                .map(payment -> PosTransactionCompletedEvent.PaymentDto.builder()
                        .paymentMode(payment.getPaymentMode().name())
                        .amount(payment.getAmount())
                        .referenceNumber(payment.getReferenceNumber())
                        .build())
                .collect(Collectors.toList());

        // Publish event for Sales, Finance, Inventory modules
        PosTransactionCompletedEvent event = new PosTransactionCompletedEvent(
                this,
                order.getOrderNo(),
                order.getTotalAmount(),
                order.getOrderDate() != null ? order.getOrderDate() : LocalDateTime.now(),
                customerId,
                warehouseId,
                lineItems,
                payments
        );

        eventPublisher.publishEvent(event);
        log.info("Published PosTransactionCompletedEvent for Restaurant Order: {}", order.getOrderNo());

        return order;
    }
}
