package org.enterprise.hr.controller;

import org.enterprise.hr.dto.EmployeeLoanDto;
import org.enterprise.hr.service.EmployeeLoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/employee-loans")
@RequiredArgsConstructor
public class EmployeeLoanController {

    private final EmployeeLoanService service;

    @PostMapping
    public ResponseEntity<EmployeeLoanDto> create(@RequestBody EmployeeLoanDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeLoanDto> update(@PathVariable Long id, @RequestBody EmployeeLoanDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeLoanDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeLoanDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
