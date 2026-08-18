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
}
