package org.enterprise.finance.controller;

import org.enterprise.finance.dto.ProfitCenterDTO;
import org.enterprise.finance.service.ProfitCenterService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/profit-centers")
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
    public ResponseEntity<ProfitCenterDTO> create(@Valid @RequestBody ProfitCenterDTO dto) {
        return ResponseEntity.ok(profitCenterService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfitCenterDTO> update(@PathVariable Long id, @Valid @RequestBody ProfitCenterDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(profitCenterService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        profitCenterService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.ProfitCenter> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return profitCenterService.searchProfitCenters(q, pageable);
    }
}
