package org.enterprise.hr.controller;

import org.enterprise.hr.dto.EmployeeExperienceDto;
import org.enterprise.hr.service.EmployeeExperienceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/employee-experiences")
@RequiredArgsConstructor
public class EmployeeExperienceController {

    private final EmployeeExperienceService service;

    @PostMapping
    public ResponseEntity<EmployeeExperienceDto> create(@RequestBody EmployeeExperienceDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeExperienceDto> update(@PathVariable Long id, @RequestBody EmployeeExperienceDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeExperienceDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeExperienceDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
