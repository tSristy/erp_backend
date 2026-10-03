package org.enterprise.sales.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.enterprise.sales.entity.DeliveryNote;
import org.enterprise.sales.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/sales/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final org.enterprise.sales.mapper.SalesMapper mapper;

    @PostMapping
    public ResponseEntity<org.enterprise.sales.dto.DeliveryNoteDto> create(@Valid @RequestBody org.enterprise.sales.dto.DeliveryNoteDto dto) {
        DeliveryNote entity = mapper.toEntity(dto);
        DeliveryNote saved = deliveryService.save(entity);
        org.enterprise.sales.dto.DeliveryNoteDto created = mapper.toDto(saved);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<org.enterprise.sales.dto.DeliveryNoteDto> confirmDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(deliveryService.confirmDelivery(id)));
    }

    @PostMapping("/{id}/create-return")
    public ResponseEntity<org.enterprise.sales.dto.DeliveryNoteDto> createReturn(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(deliveryService.createReturn(id)));
    }

    @GetMapping
    public ResponseEntity<java.util.List<org.enterprise.sales.dto.DeliveryNoteDto>> getAll() {
        return ResponseEntity.ok(mapper.toDtoListDeliveryNote(deliveryService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.DeliveryNoteDto> getById(@PathVariable Long id) {
        return deliveryService.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<org.enterprise.sales.dto.DeliveryNoteDto> update(@PathVariable Long id, @Valid @RequestBody org.enterprise.sales.dto.DeliveryNoteDto dto) {
        dto.setId(id);
        DeliveryNote entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(deliveryService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deliveryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public ResponseEntity<org.springframework.data.domain.Page<org.enterprise.sales.dto.DeliveryNoteDto>> search(
            @RequestParam(required = false) String query,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(deliveryService.search(query, pageable).map(mapper::toDto));
    }
}