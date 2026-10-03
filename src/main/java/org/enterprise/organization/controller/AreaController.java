package org.enterprise.organization.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.enterprise.organization.dto.AreaDto;
import org.enterprise.organization.entity.Area;
import org.enterprise.organization.mapper.OrganizationMapper;
import org.enterprise.organization.service.AreaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/organization/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService service;
    private final OrganizationMapper mapper;

    @GetMapping
    public ResponseEntity<List<AreaDto>> findAll() {
        return ResponseEntity.ok(mapper.toDtoListArea(service.findAll()));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AreaDto>> search(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        return ResponseEntity.ok(service.search(query, pageable).map(mapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaDto> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AreaDto> create(@Valid @RequestBody AreaDto dto) {
        Area entity = mapper.toEntity(dto);
        AreaDto createdDto = mapper.toDto(service.save(entity));
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdDto.getId())
                .toUri();
                
        return ResponseEntity.created(location).body(createdDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AreaDto> update(@PathVariable Long id, @Valid @RequestBody AreaDto dto) {
        dto.setId(id);
        Area entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(service.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
