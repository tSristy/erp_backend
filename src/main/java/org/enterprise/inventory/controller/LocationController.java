package org.enterprise.inventory.controller;

import org.enterprise.inventory.entity.Location;
import org.enterprise.inventory.service.LocationService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/inventory/locations")
public class LocationController {

    private final LocationService locationService;
    private final org.enterprise.inventory.mapper.InventoryMapper mapper;

    public LocationController(LocationService locationService, org.enterprise.inventory.mapper.InventoryMapper mapper) {
        this.locationService = locationService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<org.enterprise.inventory.dto.LocationDto> create(@Valid @RequestBody org.enterprise.inventory.dto.LocationDto dto) {
        Location entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(locationService.save(entity)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<org.enterprise.inventory.dto.LocationDto> update(@PathVariable Long id, @Valid @RequestBody org.enterprise.inventory.dto.LocationDto dto) {
        dto.setId(id);
        Location entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(locationService.save(entity)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<org.enterprise.inventory.dto.LocationDto> getById(@PathVariable Long id) {
        Optional<Location> location = locationService.findById(id);
        return location.map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        locationService.delete(id);
    }

    @GetMapping
    public ResponseEntity<List<org.enterprise.inventory.dto.LocationDto>> viewLocations() {
        return ResponseEntity.ok(mapper.toDtoListLocation(locationService.getAllLocations()));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<org.enterprise.inventory.dto.LocationDto>> getByWarehouseId(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(mapper.toDtoListLocation(locationService.getLocationsByWarehouseId(warehouseId)));
    }

    @GetMapping("/warehouse/{warehouseId}/roots")
    public ResponseEntity<List<org.enterprise.inventory.dto.LocationDto>> getRootsByWarehouseId(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(mapper.toDtoListLocation(locationService.getRootLocationsByWarehouseId(warehouseId)));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<org.enterprise.inventory.dto.LocationDto>> getByParentId(@PathVariable Long parentId) {
        return ResponseEntity.ok(mapper.toDtoListLocation(locationService.getLocationsByParentId(parentId)));
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Location> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return locationService.searchLocations(q, pageable);
    }
}
