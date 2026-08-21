package org.enterprise.production.controller;

import org.enterprise.production.dto.RoutingDto;
import org.enterprise.production.service.RoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/production/routings")
public class RoutingController {

    private final RoutingService service;

    public RoutingController(RoutingService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoutingDto> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutingDto> getById(@PathVariable Long id) {
        RoutingDto dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public RoutingDto create(@RequestBody RoutingDto dto) {
        return service.save(dto);
    }

    @PutMapping("/{id}")
    public RoutingDto update(@PathVariable Long id, @RequestBody RoutingDto dto) {
        dto.setId(id);
        return service.save(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
