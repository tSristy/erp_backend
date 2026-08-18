package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.VendorDetail;
import org.enterprise.inventory.service.VendorDetailService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor-details")
@RequiredArgsConstructor
public class VendorDetailController {

    private final VendorDetailService service;

    @PostMapping
    public VendorDetail create(@RequestBody VendorDetail entity) {
        return service.save(entity);
    }

    @GetMapping
    public List<VendorDetail> getAll(@RequestParam(required = false) Long companyId) {
        if (companyId != null) {
            return service.findByCompanyId(companyId);
        }
        return service.findAll();
    }
    
    @GetMapping("/{id}")
    public VendorDetail getById(@PathVariable Long id) {
        return service.findById(id)
                .orElseThrow(() -> new RuntimeException("VendorDetail not found"));
    }
    
    @PutMapping("/{id}")
    public VendorDetail update(@PathVariable Long id, @RequestBody VendorDetail entity) {
        entity.setId(id);
        return service.save(entity);
    }
    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
