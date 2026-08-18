package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.enterprise.inventory.enums.ProductType;
import org.enterprise.inventory.enums.CostingMethod;

import java.util.List;

@RestController
@RequestMapping({"/api/products", "/inventory/products"})
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    public Product create(@RequestBody Product entity) {
        return service.save(entity);
    }

    @GetMapping
    public List<Product> getAll() {
        return service.findAll();
    }

    @GetMapping("/search")
    public Page<Product> search(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return service.searchProducts(companyId, q, pageable);
    }
    
    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return service.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
    
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product entity) {
        entity.setId(id);
        return service.save(entity);
    }
    
    @GetMapping("/types")
    public ProductType[] getProductTypes() {
        return ProductType.values();
    }

    @GetMapping("/costing-methods")
    public CostingMethod[] getCostingMethods() {
        return CostingMethod.values();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}