package org.enterprise.hr.controller;

import org.enterprise.hr.dto.SalaryComponentDto;
import org.enterprise.hr.service.SalaryComponentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/salary-components")
@RequiredArgsConstructor
public class SalaryComponentController {

    private final SalaryComponentService service;

    @PostMapping
    public ResponseEntity<SalaryComponentDto> create(@RequestBody SalaryComponentDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalaryComponentDto> update(@PathVariable Long id, @RequestBody SalaryComponentDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaryComponentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<SalaryComponentDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
