package org.enterprise.finance.controller;

import org.enterprise.finance.dto.StatementSetupDTO;
import org.enterprise.finance.service.StatementSetupService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/statement-setups")
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
    public ResponseEntity<StatementSetupDTO> create(@Valid @RequestBody StatementSetupDTO dto) {
        return ResponseEntity.ok(statementSetupService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StatementSetupDTO> update(@PathVariable Long id, @Valid @RequestBody StatementSetupDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(statementSetupService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        statementSetupService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.StatementSetup> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return statementSetupService.searchStatementSetups(q, pageable);
    }
}
