package org.enterprise.asset.controller;

import org.enterprise.asset.entity.AssetDepreciationEntry;
import org.enterprise.asset.service.AssetDepreciationEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-management/depreciation")
public class AssetDepreciationEntryController {

    private final AssetDepreciationEntryService service;

    public AssetDepreciationEntryController(AssetDepreciationEntryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AssetDepreciationEntry>> getAll() {
        return ResponseEntity.ok(service.findAllByCurrentCompany());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetDepreciationEntry> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AssetDepreciationEntry> create(@RequestBody AssetDepreciationEntry entry) {
        entry.setId(null);
        return ResponseEntity.ok(service.save(entry));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetDepreciationEntry> update(@PathVariable Long id, @RequestBody AssetDepreciationEntry entry) {
        entry.setId(id);
        return ResponseEntity.ok(service.save(entry));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
