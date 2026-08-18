package org.enterprise.pos.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.pos.restaurant.dto.RestaurantOrderDto;
import org.enterprise.pos.restaurant.dto.RestaurantOrderDetailDto;
import org.enterprise.pos.restaurant.dto.RestaurantPaymentDto;
import org.enterprise.pos.restaurant.dto.KitchenOrderTicketDto;
import org.enterprise.pos.restaurant.entity.KitchenOrderTicket;
import org.enterprise.pos.restaurant.entity.RestaurantOrder;
import org.enterprise.pos.restaurant.entity.RestaurantOrderDetail;
import org.enterprise.pos.restaurant.entity.RestaurantPayment;
import org.enterprise.pos.restaurant.service.RestaurantOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pos/restaurant/orders")
@RequiredArgsConstructor
public class RestaurantOrderController {

    private final RestaurantOrderService orderService;

    @GetMapping
    public ResponseEntity<List<RestaurantOrderDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.findAllOrders().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/returns")
    public ResponseEntity<List<RestaurantOrderDto>> getReturnOrders() {
        return ResponseEntity.ok(orderService.findOrdersByType(RestaurantOrder.TransactionType.RETURN).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<RestaurantOrderDto> getOrder(@PathVariable Long orderId) {
        return orderService.findOrderById(orderId)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RestaurantOrderDto> createOrder(@RequestBody RestaurantOrder order) {
        return ResponseEntity.ok(mapToDto(orderService.createOrder(order)));
    }

    @PostMapping("/{orderId}/kot")
    public ResponseEntity<KitchenOrderTicketDto> sendToKitchen(
            @PathVariable Long orderId, 
            @RequestBody List<Long> detailIds) {
        return ResponseEntity.ok(mapKotToDto(orderService.sendToKitchen(orderId, detailIds)));
    }

    @PostMapping("/{orderId}/settle")
    public ResponseEntity<RestaurantOrderDto> settlePayment(@PathVariable Long orderId) {
        return ResponseEntity.ok(mapToDto(orderService.settlePayment(orderId)));
    }

    private RestaurantOrderDto mapToDto(RestaurantOrder entity) {
        if (entity == null) return null;
        RestaurantOrderDto dto = new RestaurantOrderDto();
        dto.setId(entity.getId());
        dto.setOrderNo(entity.getOrderNo());
        dto.setOrderDate(entity.getOrderDate());
        dto.setTableNumber(entity.getTableNumber());
        dto.setWaiterId(entity.getWaiterId());
        dto.setOrderType(entity.getOrderType() != null ? entity.getOrderType().name() : null);
        dto.setEventDateTime(entity.getEventDateTime());
        dto.setType(entity.getType() != null ? entity.getType().name() : null);
        dto.setReferenceOrderId(entity.getReferenceOrder() != null ? entity.getReferenceOrder().getId() : null);
        dto.setCustomerId(entity.getCustomer() != null ? entity.getCustomer().getId() : null);
        dto.setWarehouseId(entity.getWarehouse() != null ? entity.getWarehouse().getId() : null);
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setTotalAmount(entity.getTotalAmount());

        if (entity.getDetails() != null) {
            dto.setDetails(entity.getDetails().stream().map(this::mapDetailToDto).collect(Collectors.toList()));
        }
        if (entity.getPayments() != null) {
            dto.setPayments(entity.getPayments().stream().map(this::mapPaymentToDto).collect(Collectors.toList()));
        }
        if (entity.getKots() != null) {
            dto.setKots(entity.getKots().stream().map(this::mapKotToDto).collect(Collectors.toList()));
        }
        return dto;
    }

    private RestaurantOrderDetailDto mapDetailToDto(RestaurantOrderDetail detail) {
        if (detail == null) return null;
        RestaurantOrderDetailDto dto = new RestaurantOrderDetailDto();
        dto.setId(detail.getId());
        dto.setProductId(detail.getProduct() != null ? detail.getProduct().getId() : null);
        dto.setQuantity(detail.getQuantity());
        dto.setUnitPrice(detail.getUnitPrice());
        dto.setLineTotal(detail.getLineTotal());
        dto.setStatus(detail.getStatus() != null ? detail.getStatus().name() : null);
        return dto;
    }

    private RestaurantPaymentDto mapPaymentToDto(RestaurantPayment payment) {
        if (payment == null) return null;
        RestaurantPaymentDto dto = new RestaurantPaymentDto();
        dto.setId(payment.getId());
        dto.setPaymentMode(payment.getPaymentMode() != null ? payment.getPaymentMode().name() : null);
        dto.setAmount(payment.getAmount());
        dto.setTipAmount(payment.getTipAmount());
        dto.setReferenceNumber(payment.getReferenceNumber());
        return dto;
    }

    private KitchenOrderTicketDto mapKotToDto(KitchenOrderTicket kot) {
        if (kot == null) return null;
        KitchenOrderTicketDto dto = new KitchenOrderTicketDto();
        dto.setId(kot.getId());
        dto.setKotNumber(kot.getKotNumber());
        dto.setSentTime(kot.getSentTime());
        dto.setStatus(kot.getStatus() != null ? kot.getStatus().name() : null);
        dto.setOrderId(kot.getOrder() != null ? kot.getOrder().getId() : null);
        if (kot.getDetails() != null) {
            dto.setDetails(kot.getDetails().stream().map(this::mapDetailToDto).collect(Collectors.toList()));
        }
        return dto;
    }
}
