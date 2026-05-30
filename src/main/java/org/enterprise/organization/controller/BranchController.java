package org.enterprise.organization.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.organization.dto.BranchDto;
import org.enterprise.organization.service.BranchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService service;

    @PostMapping
    public ResponseEntity<BranchDto> create(@RequestBody BranchDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BranchDto> update(@PathVariable Long id, @RequestBody BranchDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<BranchDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
