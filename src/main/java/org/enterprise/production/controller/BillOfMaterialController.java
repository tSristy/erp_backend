package org.enterprise.production.controller;

import org.enterprise.production.dto.BillOfMaterialDTO;
import org.enterprise.production.service.BillOfMaterialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/production/bill-of-materials")
public class BillOfMaterialController {

    private final BillOfMaterialService service;

    public BillOfMaterialController(BillOfMaterialService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<BillOfMaterialDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillOfMaterialDTO> getById(@PathVariable Long id) {
        BillOfMaterialDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<BillOfMaterialDTO> create(@RequestBody BillOfMaterialDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BillOfMaterialDTO> update(@PathVariable Long id, @RequestBody BillOfMaterialDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
