package org.enterprise.asset.controller;

import org.enterprise.asset.entity.AssetTransfer;
import org.enterprise.asset.service.AssetTransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-management/transfers")
public class AssetTransferController {

    private final AssetTransferService service;

    public AssetTransferController(AssetTransferService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AssetTransfer>> getAll() {
        return ResponseEntity.ok(service.findAllByCurrentCompany());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetTransfer> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AssetTransfer> create(@RequestBody AssetTransfer transfer) {
        transfer.setId(null);
        return ResponseEntity.ok(service.save(transfer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetTransfer> update(@PathVariable Long id, @RequestBody AssetTransfer transfer) {
        transfer.setId(id);
        return ResponseEntity.ok(service.save(transfer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
