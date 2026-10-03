package org.enterprise.organization.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.enterprise.organization.dto.TerritoryDto;
import org.enterprise.organization.entity.Territory;
import org.enterprise.organization.mapper.OrganizationMapper;
import org.enterprise.organization.service.TerritoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/organization/territories")
@RequiredArgsConstructor
public class TerritoryController {

    private final TerritoryService service;
    private final OrganizationMapper mapper;

    @GetMapping
    public ResponseEntity<List<TerritoryDto>> findAll() {
        return ResponseEntity.ok(mapper.toDtoListTerritory(service.findAll()));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TerritoryDto>> search(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        return ResponseEntity.ok(service.search(query, pageable).map(mapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TerritoryDto> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TerritoryDto> create(@Valid @RequestBody TerritoryDto dto) {
        Territory entity = mapper.toEntity(dto);
        TerritoryDto createdDto = mapper.toDto(service.save(entity));
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdDto.getId())
                .toUri();
                
        return ResponseEntity.created(location).body(createdDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TerritoryDto> update(@PathVariable Long id, @Valid @RequestBody TerritoryDto dto) {
        dto.setId(id);
        Territory entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(service.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
