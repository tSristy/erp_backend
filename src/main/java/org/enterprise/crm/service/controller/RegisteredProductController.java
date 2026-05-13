package org.enterprise.crm.service.controller;

import org.enterprise.crm.service.entity.RegisteredProduct;
import org.enterprise.crm.service.service.RegisteredProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crm/service/registered-products")
public class RegisteredProductController {

    private final RegisteredProductService service;

    public RegisteredProductController(RegisteredProductService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RegisteredProduct>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegisteredProduct> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RegisteredProduct> create(@RequestBody RegisteredProduct product) {
        return ResponseEntity.ok(service.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegisteredProduct> update(@PathVariable Long id, @RequestBody RegisteredProduct product) {
        return service.findById(id).map(existing -> {
            product.setId(existing.getId());
            return ResponseEntity.ok(service.save(product));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
