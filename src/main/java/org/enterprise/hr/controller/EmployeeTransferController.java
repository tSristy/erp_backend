package org.enterprise.hr.controller;

import org.enterprise.hr.dto.EmployeeTransferDto;
import org.enterprise.hr.service.EmployeeTransferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/employee-transfers")
@RequiredArgsConstructor
public class EmployeeTransferController {

    private final EmployeeTransferService service;

    @PostMapping
    public ResponseEntity<EmployeeTransferDto> create(@RequestBody EmployeeTransferDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeTransferDto> update(@PathVariable Long id, @RequestBody EmployeeTransferDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeTransferDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeTransferDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
