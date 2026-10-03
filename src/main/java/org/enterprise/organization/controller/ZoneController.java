package org.enterprise.organization.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.enterprise.organization.dto.ZoneDto;
import org.enterprise.organization.entity.Zone;
import org.enterprise.organization.mapper.OrganizationMapper;
import org.enterprise.organization.service.ZoneService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/organization/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService service;
    private final OrganizationMapper mapper;

    @GetMapping
    public ResponseEntity<List<ZoneDto>> findAll() {
        return ResponseEntity.ok(mapper.toDtoListZone(service.findAll()));
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public ResponseEntity<Page<ZoneDto>> search(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        return ResponseEntity.ok(service.search(query, pageable).map(mapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneDto> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ZoneDto> create(@Valid @RequestBody ZoneDto dto) {
        Zone entity = mapper.toEntity(dto);
        ZoneDto createdDto = mapper.toDto(service.save(entity));
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdDto.getId())
                .toUri();
                
        return ResponseEntity.created(location).body(createdDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZoneDto> update(@PathVariable Long id, @Valid @RequestBody ZoneDto dto) {
        dto.setId(id);
        Zone entity = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(service.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
