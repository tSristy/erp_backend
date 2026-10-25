package org.enterprise.asset.controller;

import org.enterprise.asset.entity.AssetCategory;
import org.enterprise.asset.service.AssetCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-management/categories")
public class AssetCategoryController {

    private final AssetCategoryService service;

    public AssetCategoryController(AssetCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AssetCategory>> getAll() {
        return ResponseEntity.ok(service.findAllByCurrentCompany());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetCategory> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AssetCategory> create(@RequestBody AssetCategory category) {
        category.setId(null);
        return ResponseEntity.ok(service.save(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetCategory> update(@PathVariable Long id, @RequestBody AssetCategory category) {
        category.setId(id);
        return ResponseEntity.ok(service.save(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
