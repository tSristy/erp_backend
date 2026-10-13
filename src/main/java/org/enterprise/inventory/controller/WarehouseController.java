package org.enterprise.inventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.dto.WarehouseDto;
import org.enterprise.inventory.entity.Warehouse;
import org.enterprise.inventory.mapper.InventoryMapper;
import org.enterprise.inventory.service.WarehouseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/inventory/warehouses", "/api/v1/organization/warehouses"})
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final InventoryMapper mapper;

    @GetMapping
    public ResponseEntity<List<WarehouseDto>> findAll() {
        return ResponseEntity.ok(mapper.toDtoListWarehouse(warehouseService.findAll()));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<WarehouseDto>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        String searchQuery = (query != null && !query.trim().isEmpty()) ? query : q;
        return ResponseEntity.ok(warehouseService.search(searchQuery, pageable).map(mapper::toDto));
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<WarehouseDto> findById(@PathVariable Long id) {
        return warehouseService.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WarehouseDto> create(@Valid @RequestBody WarehouseDto dto) {
        Warehouse entity = mapper.toEntity(dto);
        WarehouseDto createdDto = mapper.toDto(warehouseService.create(entity));
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdDto.getId())
                .toUri();
                
        return ResponseEntity.created(location).body(createdDto);
    }

    @PutMapping("/{id:[0-9]+}")
    public ResponseEntity<WarehouseDto> update(@PathVariable Long id, @Valid @RequestBody WarehouseDto dto) {
        dto.setId(id);
        Warehouse entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(warehouseService.update(id, entity)));
    }

    @PutMapping
    public ResponseEntity<WarehouseDto> updateWithoutPathId(@Valid @RequestBody WarehouseDto dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("Warehouse ID is required for update");
        }
        Warehouse entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(warehouseService.update(dto.getId(), entity)));
    }

    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteByParamOrBody(@RequestParam(required = false) Long id, @RequestBody(required = false) WarehouseDto dto) {
        Long targetId = id != null ? id : (dto != null ? dto.getId() : null);
        if (targetId == null) {
            throw new IllegalArgumentException("Warehouse ID is required for delete");
        }
        warehouseService.delete(targetId);
        return ResponseEntity.noContent().build();
    }
}
