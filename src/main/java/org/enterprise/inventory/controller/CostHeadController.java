package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.CostHead;
import org.enterprise.inventory.service.CostHeadService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cost-heads")
@RequiredArgsConstructor
public class CostHeadController {

    private final CostHeadService service;

    @PostMapping
    public CostHead create(@RequestBody CostHead entity) {
        return service.save(entity);
    }

    @GetMapping
    public List<CostHead> getAll(@RequestParam(required = false) Long companyId) {
        if (companyId != null) {
            return service.findByCompanyId(companyId);
        }
        return service.findAll();
    }
    
    @GetMapping("/{id}")
    public CostHead getById(@PathVariable Long id) {
        return service.findById(id)
                .orElseThrow(() -> new RuntimeException("CostHead not found"));
    }
    
    @PutMapping("/{id}")
    public CostHead update(@PathVariable Long id, @RequestBody CostHead entity) {
        entity.setId(id);
        return service.save(entity);
    }
    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
