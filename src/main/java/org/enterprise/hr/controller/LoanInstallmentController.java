package org.enterprise.hr.controller;

import org.enterprise.hr.dto.LoanInstallmentDto;
import org.enterprise.hr.service.LoanInstallmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/loan-installments")
@RequiredArgsConstructor
public class LoanInstallmentController {

    private final LoanInstallmentService service;

    @PostMapping
    public ResponseEntity<LoanInstallmentDto> create(@RequestBody LoanInstallmentDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanInstallmentDto> update(@PathVariable Long id, @RequestBody LoanInstallmentDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanInstallmentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<LoanInstallmentDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
