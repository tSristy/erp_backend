package org.enterprise.finance.controller;

import org.enterprise.finance.dto.ProfitCenterDTO;
import org.enterprise.finance.service.ProfitCenterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/profit-centers")
public class ProfitCenterController {

    private final ProfitCenterService profitCenterService;

    public ProfitCenterController(ProfitCenterService profitCenterService) {
        this.profitCenterService = profitCenterService;
    }

    @GetMapping
    public ResponseEntity<List<ProfitCenterDTO>> getAll() {
        return ResponseEntity.ok(profitCenterService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfitCenterDTO> getById(@PathVariable Long id) {
        ProfitCenterDTO dto = profitCenterService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ProfitCenterDTO> create(@RequestBody ProfitCenterDTO dto) {
        return ResponseEntity.ok(profitCenterService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfitCenterDTO> update(@PathVariable Long id, @RequestBody ProfitCenterDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(profitCenterService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        profitCenterService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
