package org.enterprise.hr.controller;

import org.enterprise.hr.dto.EmployeePromotionDto;
import org.enterprise.hr.service.EmployeePromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/employee-promotions")
@RequiredArgsConstructor
public class EmployeePromotionController {

    private final EmployeePromotionService service;

    @PostMapping
    public ResponseEntity<EmployeePromotionDto> create(@RequestBody EmployeePromotionDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeePromotionDto> update(@PathVariable Long id, @RequestBody EmployeePromotionDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeePromotionDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeePromotionDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
