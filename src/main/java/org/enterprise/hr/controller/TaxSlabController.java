package org.enterprise.hr.controller;

import org.enterprise.hr.dto.TaxSlabDto;
import org.enterprise.hr.service.TaxSlabService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/tax-slabs")
@RequiredArgsConstructor
public class TaxSlabController {

    private final TaxSlabService service;

    @PostMapping
    public ResponseEntity<TaxSlabDto> create(@RequestBody TaxSlabDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaxSlabDto> update(@PathVariable Long id, @RequestBody TaxSlabDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaxSlabDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<TaxSlabDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
