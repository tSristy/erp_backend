package org.enterprise.crm.service.controller;

import org.enterprise.crm.service.entity.MaintenanceSchedule;
import org.enterprise.crm.service.service.MaintenanceScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crm/service/maintenance-schedules")
public class MaintenanceScheduleController {

    private final MaintenanceScheduleService service;

    public MaintenanceScheduleController(MaintenanceScheduleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceSchedule>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceSchedule> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MaintenanceSchedule> create(@RequestBody MaintenanceSchedule schedule) {
        return ResponseEntity.ok(service.save(schedule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceSchedule> update(@PathVariable Long id, @RequestBody MaintenanceSchedule schedule) {
        return service.findById(id).map(existing -> {
            schedule.setId(existing.getId());
            return ResponseEntity.ok(service.save(schedule));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
