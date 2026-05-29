package org.enterprise.hr.controller;

import org.enterprise.hr.dto.EmployeeRosterDto;
import org.enterprise.hr.service.EmployeeRosterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/employee-rosters")
@RequiredArgsConstructor
public class EmployeeRosterController {

    private final EmployeeRosterService service;

    @PostMapping
    public ResponseEntity<EmployeeRosterDto> create(@RequestBody EmployeeRosterDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeRosterDto> update(@PathVariable Long id, @RequestBody EmployeeRosterDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeRosterDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeRosterDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
