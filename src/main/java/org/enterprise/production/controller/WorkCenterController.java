package org.enterprise.production.controller;

import org.enterprise.production.dto.WorkCenterDto;
import org.enterprise.production.service.WorkCenterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/production/work-centers")
public class WorkCenterController {

    private final WorkCenterService service;

    public WorkCenterController(WorkCenterService service) {
        this.service = service;
    }

    @GetMapping
    public List<WorkCenterDto> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkCenterDto> getById(@PathVariable Long id) {
        WorkCenterDto dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public WorkCenterDto create(@RequestBody WorkCenterDto dto) {
        return service.save(dto);
    }

    @PutMapping("/{id}")
    public WorkCenterDto update(@PathVariable Long id, @RequestBody WorkCenterDto dto) {
        dto.setId(id);
        return service.save(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
