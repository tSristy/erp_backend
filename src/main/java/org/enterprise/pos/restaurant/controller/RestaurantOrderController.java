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
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pos/restaurant/orders")
@RequiredArgsConstructor
public class RestaurantOrderController {

    private final RestaurantOrderService orderService;
    private final org.enterprise.hr.repository.EmployeeRepository employeeRepository;

    @GetMapping
    public ResponseEntity<List<RestaurantOrderDto>> getAllOrders(
            @RequestParam(required = false) Long warehouseId) {
        List<RestaurantOrder> orders = warehouseId != null
                ? orderService.findOrdersByWarehouseId(warehouseId)
                : orderService.findAllOrders();
        return ResponseEntity.ok(orders.stream()
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<RestaurantOrderDto> getOrder(@PathVariable Long orderId) {
        return orderService.findOrderById(orderId)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RestaurantOrderDto> createOrder(@Valid @RequestBody RestaurantOrder order) {
        RestaurantOrderDto created = mapToDto(orderService.createOrder(order));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/{orderId}/kot")
    public ResponseEntity<KitchenOrderTicketDto> sendToKitchen(
            @PathVariable Long orderId, 
            @RequestBody List<Long> detailIds) {
        return ResponseEntity.ok(mapKotToDto(orderService.sendToKitchen(orderId, detailIds)));
    }

    @PostMapping("/{orderId}/settle")
    public ResponseEntity<RestaurantOrderDto> settlePayment(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, Object> body) {
        String paymentMode = "CASH";
        BigDecimal amount = null;
        BigDecimal tipAmount = null;
        String referenceNumber = null;

        if (body != null) {
            if (body.containsKey("paymentMode") && body.get("paymentMode") != null) {
                paymentMode = body.get("paymentMode").toString();
            }
            if (body.containsKey("amount") && body.get("amount") != null) {
                try {
                    amount = new BigDecimal(body.get("amount").toString());
                } catch (Exception ignored) {}
            }
            if (body.containsKey("tipAmount") && body.get("tipAmount") != null) {
                try {
                    tipAmount = new BigDecimal(body.get("tipAmount").toString());
                } catch (Exception ignored) {}
            }
            if (body.containsKey("referenceNumber") && body.get("referenceNumber") != null) {
                referenceNumber = body.get("referenceNumber").toString();
            }
        }

        return ResponseEntity.ok(mapToDto(orderService.settlePayment(orderId, paymentMode, amount, tipAmount, referenceNumber)));
    }

    @RequestMapping(value = "/{orderId}/waiter", method = {RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<RestaurantOrderDto> assignWaiter(
            @PathVariable Long orderId, 
            @RequestBody(required = false) java.util.Map<String, Object> body) {
        Long waiterId = null;
        if (body != null && body.containsKey("waiterId")) {
            Object wId = body.get("waiterId");
            if (wId instanceof Number) {
                waiterId = ((Number) wId).longValue();
            } else if (wId != null && !wId.toString().trim().isEmpty()) {
                try {
                    waiterId = Long.valueOf(wId.toString().trim());
                } catch (NumberFormatException ignored) {}
            }
        }
        return ResponseEntity.ok(mapToDto(orderService.assignWaiter(orderId, waiterId)));
    }

    private RestaurantOrderDto mapToDto(RestaurantOrder entity) {
        if (entity == null) return null;
        RestaurantOrderDto dto = new RestaurantOrderDto();
        dto.setId(entity.getId());
        dto.setOrderNo(entity.getOrderNo());
        dto.setOrderDate(entity.getOrderDate());
        dto.setTableNumber(entity.getTableNumber());
        dto.setWaiterId(entity.getWaiterId());
        if (entity.getWaiterId() != null) {
            try {
                employeeRepository.findById(entity.getWaiterId()).ifPresent(emp -> {
                    String name = emp.getFullName();
                    if (name == null || name.trim().isEmpty()) {
                        name = emp.getFirstName() != null ? emp.getFirstName() + " " + (emp.getLastName() != null ? emp.getLastName() : "") : emp.getEmployeeCode();
                    }
                    dto.setWaiterName(name);
                });
            } catch (Exception ignored) {}
        }
        dto.setOrderType(entity.getOrderType() != null ? entity.getOrderType().name() : null);
        dto.setEventDateTime(entity.getEventDateTime());
        dto.setType(entity.getType() != null ? entity.getType().name() : null);
        dto.setReferenceOrderId(entity.getReferenceOrder() != null ? entity.getReferenceOrder().getId() : null);
        dto.setCustomerId(entity.getCustomerId() != null ? entity.getCustomerId() : (entity.getCustomer() != null ? entity.getCustomer().getId() : null));
        if (entity.getCustomer() != null) {
            try {
                dto.setCustomerName(entity.getCustomer().getName());
            } catch (Exception ignored) {}
        } else {
            dto.setCustomerName("Walk-in Customer");
        }
        dto.setWarehouseId(entity.getWarehouseId() != null ? entity.getWarehouseId() : (entity.getWarehouse() != null ? entity.getWarehouse().getId() : null));
        if (entity.getWarehouse() != null) {
            try {
                dto.setWarehouseName(entity.getWarehouse().getName());
            } catch (Exception ignored) {}
        }
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setTotalAmount(entity.getTotalAmount());

        if (entity.getDetails() != null) {
            dto.setDetails(entity.getDetails().stream().map(this::mapDetailToDto).collect(Collectors.toList()));
        }
        if (entity.getPayments() != null && !entity.getPayments().isEmpty()) {
            dto.setPayments(entity.getPayments().stream().map(this::mapPaymentToDto).collect(Collectors.toList()));
        } else if (entity.getStatus() == RestaurantOrder.RestaurantOrderStatus.PAID
                && entity.getTotalAmount() != null
                && entity.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            // Auto-heal existing PAID orders that were settled before payments were explicitly persisted
            RestaurantPaymentDto autoPayment = new RestaurantPaymentDto();
            autoPayment.setPaymentMode("CASH");
            autoPayment.setAmount(entity.getTotalAmount());
            autoPayment.setReferenceNumber("SETTLED");
            dto.setPayments(Collections.singletonList(autoPayment));
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
        Long prodId = detail.getProductId();
        String prodName = null;
        if (detail.getProduct() != null) {
            try {
                if (prodId == null) prodId = detail.getProduct().getId();
                prodName = detail.getProduct().getName();
            } catch (Exception ignored) {}
        }
        dto.setProductId(prodId);
        dto.setProductName(prodName);
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

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.pos.restaurant.entity.RestaurantOrder> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return orderService.searchRestaurantOrderDtos(org.enterprise.common.util.TenantContext.getCompanyId(), q, pageable);
    }

}