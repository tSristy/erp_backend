package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.Tax;
import org.enterprise.inventory.service.TaxService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/taxes")
@RequiredArgsConstructor
public class TaxController {

    private final TaxService service;

    @PostMapping
    public Tax create(@RequestBody Tax entity) {
        return service.save(entity);
    }

    @GetMapping
    public List<Tax> getAll(@RequestParam(required = false) Long companyId) {
        if (companyId != null) {
            return service.findByCompanyId(companyId);
        }
        return service.findAll();
    }
    
    @GetMapping("/{id}")
    public Tax getById(@PathVariable Long id) {
        return service.findById(id)
                .orElseThrow(() -> new RuntimeException("Tax not found"));
    }
    
    @PutMapping("/{id}")
    public Tax update(@PathVariable Long id, @RequestBody Tax entity) {
        entity.setId(id);
        return service.save(entity);
    }
    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Tax> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.searchTaxs(q, pageable);
    }
}
