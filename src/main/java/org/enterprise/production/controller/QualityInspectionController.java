package org.enterprise.production.controller;

import jakarta.validation.Valid;
import org.enterprise.common.util.TenantContext;
import org.enterprise.production.dto.QualityInspectionDTO;
import org.enterprise.production.service.QualityInspectionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/production/quality-inspections")
public class QualityInspectionController {

    private final QualityInspectionService service;

    public QualityInspectionController(QualityInspectionService service) {
        this.service = service;
    }

    @GetMapping
    public List<QualityInspectionDTO> getAll(@RequestParam(required = false) Long manufacturingOrderId) {
        if (manufacturingOrderId != null) {
            return service.findByManufacturingOrderId(manufacturingOrderId);
        }
        return service.findAll();
    }

    @GetMapping("/next-number")
    public ResponseEntity<Map<String, String>> getNextNumber() {
        return ResponseEntity.ok(Map.of("inspectionNo", service.generateNextInspectionNo()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QualityInspectionDTO> getById(@PathVariable Long id) {
        QualityInspectionDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<QualityInspectionDTO> create(@Valid @RequestBody QualityInspectionDTO dto) {
        QualityInspectionDTO saved = service.save(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QualityInspectionDTO> update(@PathVariable Long id, @Valid @RequestBody QualityInspectionDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public Page<QualityInspectionDTO> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return service.search(TenantContext.getCompanyId(), pageable);
    }
}
