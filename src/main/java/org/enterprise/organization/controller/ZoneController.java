package org.enterprise.organization.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.organization.dto.ZoneDto;
import org.enterprise.organization.service.ZoneService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService service;

    @PostMapping
    public ResponseEntity<ZoneDto> create(@RequestBody ZoneDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZoneDto> update(@PathVariable Long id, @RequestBody ZoneDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ZoneDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
