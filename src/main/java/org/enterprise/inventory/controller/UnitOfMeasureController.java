package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.UnitOfMeasure;
import org.enterprise.inventory.service.UnitOfMeasureService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/unit-of-measures")
@RequiredArgsConstructor
public class UnitOfMeasureController {

    private final UnitOfMeasureService service;

    @PostMapping
    public UnitOfMeasure create(@RequestBody UnitOfMeasure entity) {
        return service.save(entity);
    }

    @GetMapping
    public List<UnitOfMeasure> getAll(@RequestParam(required = false) Long companyId) {
        if (companyId != null) {
            return service.findByCompanyId(companyId);
        }
        return service.findAll();
    }
    
    @GetMapping("/{id}")
    public UnitOfMeasure getById(@PathVariable Long id) {
        return service.findById(id)
                .orElseThrow(() -> new RuntimeException("UnitOfMeasure not found"));
    }
    
    @PutMapping("/{id}")
    public UnitOfMeasure update(@PathVariable Long id, @RequestBody UnitOfMeasure entity) {
        entity.setId(id);
        return service.save(entity);
    }
    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.UnitOfMeasure> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.searchUnitOfMeasures(q, pageable);
    }
}
