package org.enterprise.hr.controller;

import org.enterprise.hr.dto.EmployeeDto;
import org.enterprise.hr.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    @PostMapping
    public ResponseEntity<EmployeeDto> create(
            @RequestBody EmployeeDto dto) {

        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {

        return ResponseEntity.ok(service.search(keyword, pageable));
    }
}