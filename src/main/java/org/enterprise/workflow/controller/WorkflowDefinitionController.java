package org.enterprise.workflow.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.workflow.dto.WorkflowDefinitionDto;
import org.enterprise.workflow.entity.WorkflowDefinition;
import org.enterprise.workflow.service.WorkflowDefinitionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflows/definitions")
@RequiredArgsConstructor
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService service;

    @GetMapping
    @PreAuthorize("hasAuthority('WORKFLOW_READ')")
    public ResponseEntity<List<WorkflowDefinitionDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKFLOW_READ')")
    public ResponseEntity<WorkflowDefinitionDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WORKFLOW_WRITE')")
    public ResponseEntity<WorkflowDefinitionDto> create(@RequestBody WorkflowDefinitionDto dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKFLOW_WRITE')")
    public ResponseEntity<WorkflowDefinitionDto> update(@PathVariable Long id, @RequestBody WorkflowDefinitionDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKFLOW_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<WorkflowDefinitionDto> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long companyId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.searchWorkflowDefinitionDtos(companyId, q, pageable);
    }

}