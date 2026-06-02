package org.enterprise.pos.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.pos.restaurant.entity.KitchenOrderTicket;
import org.enterprise.pos.restaurant.entity.RestaurantOrder;
import org.enterprise.pos.restaurant.service.RestaurantOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos/restaurant/orders")
@RequiredArgsConstructor
public class RestaurantOrderController {

    private final RestaurantOrderService orderService;

    @PostMapping
    public ResponseEntity<RestaurantOrder> createOrder(@RequestBody RestaurantOrder order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @PostMapping("/{orderId}/kot")
    public ResponseEntity<KitchenOrderTicket> sendToKitchen(
            @PathVariable Long orderId, 
            @RequestBody List<Long> detailIds) {
        return ResponseEntity.ok(orderService.sendToKitchen(orderId, detailIds));
    }

    @PostMapping("/{orderId}/settle")
    public ResponseEntity<RestaurantOrder> settlePayment(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.settlePayment(orderId));
    }
}
