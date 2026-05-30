package org.enterprise.organization.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.organization.dto.AreaDto;
import org.enterprise.organization.service.AreaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService service;

    @PostMapping
    public ResponseEntity<AreaDto> create(@RequestBody AreaDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AreaDto> update(@PathVariable Long id, @RequestBody AreaDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<AreaDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
