package org.enterprise.hr.controller;

import org.enterprise.hr.dto.DesignationDto;
import org.enterprise.hr.service.DesignationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService service;

    @PostMapping
    public ResponseEntity<DesignationDto> create(@RequestBody DesignationDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DesignationDto> update(@PathVariable Long id, @RequestBody DesignationDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesignationDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<DesignationDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.hr.dto.DesignationDto> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long companyId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.searchDesignations(companyId, q, pageable);
    }

}