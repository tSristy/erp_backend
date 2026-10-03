package org.enterprise.finance.controller;

import org.enterprise.finance.dto.CostCenterDTO;
import org.enterprise.finance.service.CostCenterService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/cost-centers")
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
    public ResponseEntity<CostCenterDTO> create(@Valid @RequestBody CostCenterDTO dto) {
        return ResponseEntity.ok(costCenterService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CostCenterDTO> update(@PathVariable Long id, @Valid @RequestBody CostCenterDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(costCenterService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        costCenterService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.CostCenter> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return costCenterService.searchCostCenters(q, pageable);
    }
}
