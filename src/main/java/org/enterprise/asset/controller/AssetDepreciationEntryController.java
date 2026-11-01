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

    @PostMapping("/bulk-run")
    public ResponseEntity<List<AssetDepreciationEntry>> bulkRun(
            @RequestBody(required = false) java.util.Map<String, Object> body) {
        java.time.LocalDate periodDate = java.time.LocalDate.now();
        String frequency = "MONTHLY";
        String remarks = null;
        java.util.List<Long> assetIds = null;

        if (body != null) {
            if (body.get("periodDate") != null && !body.get("periodDate").toString().isBlank()) {
                periodDate = java.time.LocalDate.parse(body.get("periodDate").toString().substring(0, 10));
            }
            if (body.get("frequency") != null) {
                frequency = body.get("frequency").toString();
            }
            if (body.get("remarks") != null) {
                remarks = body.get("remarks").toString();
            }
            if (body.get("assetIds") instanceof java.util.List<?> rawIds) {
                assetIds = rawIds.stream()
                        .filter(java.util.Objects::nonNull)
                        .map(o -> Long.valueOf(o.toString()))
                        .toList();
            }
        }

        return ResponseEntity.ok(service.runBulkDepreciation(periodDate, frequency, remarks, assetIds));
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
