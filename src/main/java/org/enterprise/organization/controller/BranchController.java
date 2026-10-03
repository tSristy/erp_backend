package org.enterprise.organization.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.enterprise.organization.dto.BranchDto;
import org.enterprise.organization.entity.Branch;
import org.enterprise.organization.mapper.OrganizationMapper;
import org.enterprise.organization.service.BranchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/organization/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService service;
    private final OrganizationMapper mapper;

    @GetMapping
    public ResponseEntity<List<BranchDto>> findAll() {
        return ResponseEntity.ok(mapper.toDtoListBranch(service.findAll()));
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public ResponseEntity<Page<BranchDto>> search(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        return ResponseEntity.ok(service.search(query, pageable).map(mapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchDto> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BranchDto> create(@Valid @RequestBody BranchDto dto) {
        Branch entity = mapper.toEntity(dto);
        BranchDto createdDto = mapper.toDto(service.save(entity));
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdDto.getId())
                .toUri();
                
        return ResponseEntity.created(location).body(createdDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BranchDto> update(@PathVariable Long id, @Valid @RequestBody BranchDto dto) {
        dto.setId(id);
        Branch entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(service.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
