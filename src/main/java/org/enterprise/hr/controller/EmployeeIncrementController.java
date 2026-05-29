package org.enterprise.hr.controller;

import org.enterprise.hr.dto.EmployeeIncrementDto;
import org.enterprise.hr.service.EmployeeIncrementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/employee-increments")
@RequiredArgsConstructor
public class EmployeeIncrementController {

    private final EmployeeIncrementService service;

    @PostMapping
    public ResponseEntity<EmployeeIncrementDto> create(@RequestBody EmployeeIncrementDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeIncrementDto> update(@PathVariable Long id, @RequestBody EmployeeIncrementDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeIncrementDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeIncrementDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
