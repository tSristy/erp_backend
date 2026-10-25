package org.enterprise.asset.controller;

import org.enterprise.asset.entity.AssetDisposal;
import org.enterprise.asset.service.AssetDisposalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-management/disposals")
public class AssetDisposalController {

    private final AssetDisposalService service;

    public AssetDisposalController(AssetDisposalService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AssetDisposal>> getAll() {
        return ResponseEntity.ok(service.findAllByCurrentCompany());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetDisposal> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AssetDisposal> create(@RequestBody AssetDisposal disposal) {
        disposal.setId(null);
        return ResponseEntity.ok(service.save(disposal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetDisposal> update(@PathVariable Long id, @RequestBody AssetDisposal disposal) {
        disposal.setId(id);
        return ResponseEntity.ok(service.save(disposal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
