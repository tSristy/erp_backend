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

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.pos.restaurant.entity.KitchenOrderTicket> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long companyId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return kotService.searchKitchenOrderTicketDtos(companyId, q, pageable);
    }

}