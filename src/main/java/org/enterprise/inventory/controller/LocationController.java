package org.enterprise.inventory.controller;

import org.enterprise.inventory.entity.Location;
import org.enterprise.inventory.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory/locations")
public class LocationController {

    private final LocationService locationService;
    private final org.enterprise.inventory.mapper.InventoryMapper mapper;

    public LocationController(LocationService locationService, org.enterprise.inventory.mapper.InventoryMapper mapper) {
        this.locationService = locationService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<org.enterprise.inventory.dto.LocationDto> create(@RequestBody org.enterprise.inventory.dto.LocationDto dto) {
        Location entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(locationService.save(entity)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<org.enterprise.inventory.dto.LocationDto> update(@PathVariable Long id, @RequestBody org.enterprise.inventory.dto.LocationDto dto) {
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
}
