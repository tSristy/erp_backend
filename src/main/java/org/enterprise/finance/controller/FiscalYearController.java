package org.enterprise.finance.controller;

import org.enterprise.finance.dto.FiscalYearDTO;
import org.enterprise.finance.service.FiscalYearService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/fiscal-years")
public class FiscalYearController {

    private final FiscalYearService fiscalYearService;

    public FiscalYearController(FiscalYearService fiscalYearService) {
        this.fiscalYearService = fiscalYearService;
    }

    @GetMapping
    public ResponseEntity<List<FiscalYearDTO>> getAll() {
        return ResponseEntity.ok(fiscalYearService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FiscalYearDTO> getById(@PathVariable Long id) {
        FiscalYearDTO dto = fiscalYearService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<FiscalYearDTO> create(@RequestBody FiscalYearDTO dto) {
        return ResponseEntity.ok(fiscalYearService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FiscalYearDTO> update(@PathVariable Long id, @RequestBody FiscalYearDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(fiscalYearService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fiscalYearService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
