package org.enterprise.workflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.enterprise.workflow.dto.WorkflowDefinitionDto;
import org.enterprise.workflow.entity.WorkflowDefinition;
import org.enterprise.workflow.mapper.WorkflowMapper;
import org.enterprise.workflow.service.WorkflowDefinitionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/workflow/definitions")
@RequiredArgsConstructor
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService service;
    private final WorkflowMapper mapper;

    @GetMapping
    @PreAuthorize("hasAuthority('WORKFLOW_READ')")
    public ResponseEntity<List<WorkflowDefinitionDto>> findAll() {
        return ResponseEntity.ok(mapper.toDtoList(service.findAll()));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<WorkflowDefinitionDto>> search(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        return ResponseEntity.ok(service.search(query, pageable).map(mapper::toDto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKFLOW_READ')")
    public ResponseEntity<WorkflowDefinitionDto> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WORKFLOW_WRITE')")
    public ResponseEntity<WorkflowDefinitionDto> create(@Valid @RequestBody WorkflowDefinitionDto dto) {
        WorkflowDefinition entity = mapper.toEntity(dto, new WorkflowDefinition());
        WorkflowDefinitionDto createdDto = mapper.toDto(service.save(entity));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdDto.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKFLOW_WRITE')")
    public ResponseEntity<WorkflowDefinitionDto> update(@PathVariable Long id, @Valid @RequestBody WorkflowDefinitionDto dto) {
        dto.setId(id);
        WorkflowDefinition existingEntity = service.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow Definition not found"));
        WorkflowDefinition updatedEntity = mapper.toEntity(dto, existingEntity);
        return ResponseEntity.ok(mapper.toDto(service.save(updatedEntity)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKFLOW_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
