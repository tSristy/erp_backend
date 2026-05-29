package org.enterprise.hr.controller;

import org.enterprise.hr.dto.EmployeeConfirmationDto;
import org.enterprise.hr.service.EmployeeConfirmationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/employee-confirmations")
@RequiredArgsConstructor
public class EmployeeConfirmationController {

    private final EmployeeConfirmationService service;

    @PostMapping
    public ResponseEntity<EmployeeConfirmationDto> create(@RequestBody EmployeeConfirmationDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeConfirmationDto> update(@PathVariable Long id, @RequestBody EmployeeConfirmationDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeConfirmationDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeConfirmationDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
