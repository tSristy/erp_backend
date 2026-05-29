package org.enterprise.hr.controller;

import org.enterprise.hr.dto.EmployeeEducationDto;
import org.enterprise.hr.service.EmployeeEducationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/employee-educations")
@RequiredArgsConstructor
public class EmployeeEducationController {

    private final EmployeeEducationService service;

    @PostMapping
    public ResponseEntity<EmployeeEducationDto> create(@RequestBody EmployeeEducationDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeEducationDto> update(@PathVariable Long id, @RequestBody EmployeeEducationDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeEducationDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeEducationDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
