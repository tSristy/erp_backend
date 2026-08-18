package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.UnitOfMeasure;
import org.enterprise.inventory.service.UnitOfMeasureService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unit-of-measures")
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
}
