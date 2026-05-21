package org.enterprise.finance.controller;

import org.enterprise.finance.dto.StatementSetupDTO;
import org.enterprise.finance.service.StatementSetupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/statement-setups")
public class StatementSetupController {

    private final StatementSetupService statementSetupService;

    public StatementSetupController(StatementSetupService statementSetupService) {
        this.statementSetupService = statementSetupService;
    }

    @GetMapping
    public ResponseEntity<List<StatementSetupDTO>> getAll() {
        return ResponseEntity.ok(statementSetupService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StatementSetupDTO> getById(@PathVariable Long id) {
        StatementSetupDTO dto = statementSetupService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<StatementSetupDTO> create(@RequestBody StatementSetupDTO dto) {
        return ResponseEntity.ok(statementSetupService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StatementSetupDTO> update(@PathVariable Long id, @RequestBody StatementSetupDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(statementSetupService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        statementSetupService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
