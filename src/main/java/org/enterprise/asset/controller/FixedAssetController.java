package org.enterprise.asset.controller;

import org.enterprise.asset.entity.FixedAsset;
import org.enterprise.asset.service.FixedAssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-management/assets")
public class FixedAssetController {

    private final FixedAssetService service;

    public FixedAssetController(FixedAssetService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<FixedAsset>> getAll() {
        return ResponseEntity.ok(service.findAllByCurrentCompany());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FixedAsset> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FixedAsset> create(@RequestBody FixedAsset asset) {
        asset.setId(null);
        return ResponseEntity.ok(service.save(asset));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FixedAsset> update(@PathVariable Long id, @RequestBody FixedAsset asset) {
        asset.setId(id);
        return ResponseEntity.ok(service.save(asset));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
