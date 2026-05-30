package org.enterprise.organization.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.organization.dto.TerritoryDto;
import org.enterprise.organization.service.TerritoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization/territories")
@RequiredArgsConstructor
public class TerritoryController {

    private final TerritoryService service;

    @PostMapping
    public ResponseEntity<TerritoryDto> create(@RequestBody TerritoryDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TerritoryDto> update(@PathVariable Long id, @RequestBody TerritoryDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TerritoryDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<TerritoryDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
