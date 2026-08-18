package org.enterprise.pos.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.pos.restaurant.dto.KitchenOrderTicketDto;
import org.enterprise.pos.restaurant.dto.RestaurantOrderDetailDto;
import org.enterprise.pos.restaurant.entity.KitchenOrderTicket;
import org.enterprise.pos.restaurant.entity.RestaurantOrderDetail;
import org.enterprise.pos.restaurant.service.KitchenOrderTicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pos/restaurant/kots")
@RequiredArgsConstructor
public class KitchenOrderTicketController {

    private final KitchenOrderTicketService kotService;

    @GetMapping("/pending")
    public ResponseEntity<List<KitchenOrderTicketDto>> getPendingKots() {
        return ResponseEntity.ok(kotService.findAllPendingKots().stream()
                .map(this::mapKotToDto)
                .collect(Collectors.toList()));
    }

    @PostMapping("/{kotId}/serve")
    public ResponseEntity<KitchenOrderTicketDto> markAsServed(@PathVariable Long kotId) {
        return ResponseEntity.ok(mapKotToDto(kotService.markAsServed(kotId)));
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
}
