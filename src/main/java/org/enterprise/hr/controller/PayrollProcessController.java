package org.enterprise.hr.controller;

import org.enterprise.hr.dto.PayrollProcessDto;
import org.enterprise.hr.service.PayrollProcessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/payroll-processs")
@RequiredArgsConstructor
public class PayrollProcessController {

    private final PayrollProcessService service;

    @PostMapping
    public ResponseEntity<PayrollProcessDto> create(@RequestBody PayrollProcessDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayrollProcessDto> update(@PathVariable Long id, @RequestBody PayrollProcessDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollProcessDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PayrollProcessDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
