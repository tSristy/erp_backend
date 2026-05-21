package org.enterprise.finance.controller;

import org.enterprise.finance.dto.CostCenterDTO;
import org.enterprise.finance.service.CostCenterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/cost-centers")
public class CostCenterController {

    private final CostCenterService costCenterService;

    public CostCenterController(CostCenterService costCenterService) {
        this.costCenterService = costCenterService;
    }

    @GetMapping
    public ResponseEntity<List<CostCenterDTO>> getAll() {
        return ResponseEntity.ok(costCenterService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CostCenterDTO> getById(@PathVariable Long id) {
        CostCenterDTO dto = costCenterService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<CostCenterDTO> create(@RequestBody CostCenterDTO dto) {
        return ResponseEntity.ok(costCenterService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CostCenterDTO> update(@PathVariable Long id, @RequestBody CostCenterDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(costCenterService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        costCenterService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
