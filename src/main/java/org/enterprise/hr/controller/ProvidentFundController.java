package org.enterprise.hr.controller;

import org.enterprise.hr.dto.ProvidentFundDto;
import org.enterprise.hr.service.ProvidentFundService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/provident-funds")
@RequiredArgsConstructor
public class ProvidentFundController {

    private final ProvidentFundService service;

    @PostMapping
    public ResponseEntity<ProvidentFundDto> create(@RequestBody ProvidentFundDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProvidentFundDto> update(@PathVariable Long id, @RequestBody ProvidentFundDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProvidentFundDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProvidentFundDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
