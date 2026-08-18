package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.Warehouse;
import org.enterprise.inventory.service.WarehouseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final org.enterprise.inventory.mapper.InventoryMapper mapper;

    @GetMapping
    public ResponseEntity<List<org.enterprise.inventory.dto.WarehouseDto>> findAll() {
        return ResponseEntity.ok(mapper.toDtoListWarehouse(warehouseService.findAll()));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<org.enterprise.inventory.dto.WarehouseDto>> search(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        return ResponseEntity.ok(warehouseService.search(query, pageable).map(mapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<org.enterprise.inventory.dto.WarehouseDto> findById(@PathVariable Long id) {
        return warehouseService.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<org.enterprise.inventory.dto.WarehouseDto> create(@RequestBody org.enterprise.inventory.dto.WarehouseDto dto) {
        Warehouse entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(warehouseService.save(entity)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<org.enterprise.inventory.dto.WarehouseDto> update(@PathVariable Long id, @RequestBody org.enterprise.inventory.dto.WarehouseDto dto) {
        dto.setId(id);
        Warehouse entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(warehouseService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return ResponseEntity.ok().build();
    }
}
