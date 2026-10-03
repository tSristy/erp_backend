package org.enterprise.finance.controller;

import org.enterprise.finance.dto.FiscalYearDTO;
import org.enterprise.finance.service.FiscalYearService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/fiscal-years")
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
    public ResponseEntity<FiscalYearDTO> create(@Valid @RequestBody FiscalYearDTO dto) {
        return ResponseEntity.ok(fiscalYearService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FiscalYearDTO> update(@PathVariable Long id, @Valid @RequestBody FiscalYearDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(fiscalYearService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fiscalYearService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.FiscalYear> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return fiscalYearService.searchFiscalYears(q, pageable);
    }
}
