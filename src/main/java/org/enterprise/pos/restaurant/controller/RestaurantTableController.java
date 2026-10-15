package org.enterprise.pos.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.pos.restaurant.dto.RestaurantTableDto;
import org.enterprise.pos.restaurant.entity.RestaurantTable;
import org.enterprise.pos.restaurant.service.RestaurantTableService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pos/restaurant/tables")
@RequiredArgsConstructor
public class RestaurantTableController {

    private final RestaurantTableService service;

    @GetMapping
    public ResponseEntity<List<RestaurantTableDto>> getTables(
            @RequestParam(required = false) Long warehouseId) {
        List<RestaurantTableDto> dtos = service.getTablesByOutlet(warehouseId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantTableDto> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RestaurantTableDto> create(@RequestBody RestaurantTable table) {
        RestaurantTable saved = service.save(table);
        return ResponseEntity.ok(mapToDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantTableDto> update(@PathVariable Long id, @RequestBody RestaurantTable table) {
        return service.findById(id).map(existing -> {
            table.setId(existing.getId());
            if (table.getCompanyId() == null) {
                table.setCompanyId(existing.getCompanyId());
            }
            RestaurantTable updated = service.save(table);
            return ResponseEntity.ok(mapToDto(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<RestaurantTableDto>> search(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        String effectiveQuery = (query != null && !query.trim().isEmpty()) ? query : q;
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RestaurantTableDto> pageDto = service.search(warehouseId, effectiveQuery, pageable).map(this::mapToDto);
        return ResponseEntity.ok(pageDto);
    }

    private RestaurantTableDto mapToDto(RestaurantTable entity) {
        if (entity == null) return null;
        RestaurantTableDto dto = new RestaurantTableDto();
        dto.setId(entity.getId());
        dto.setTableNumber(entity.getTableNumber());
        dto.setName(entity.getName());
        dto.setCapacity(entity.getCapacity());
        dto.setSection(entity.getSection());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setActive(entity.getActive());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setWarehouseId(entity.getWarehouse() != null ? entity.getWarehouse().getId() : entity.getWarehouseId());
        try {
            if (entity.getWarehouse() != null) {
                dto.setWarehouseName(entity.getWarehouse().getName());
            }
        } catch (Exception ignored) {
            // Lazy loading safety
        }
        return dto;
    }
}
