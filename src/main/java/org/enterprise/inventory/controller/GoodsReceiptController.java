package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.dto.GoodsReceiptRequestDto;
import org.enterprise.inventory.service.GoodsReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grn")
@RequiredArgsConstructor
public class GoodsReceiptController {

    private final GoodsReceiptService service;

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody GoodsReceiptRequestDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<?> post(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.post(id));
    }

    @PostMapping("/{id}/create-return")
    public ResponseEntity<?> createReturn(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.createReturn(id));
    }
}
