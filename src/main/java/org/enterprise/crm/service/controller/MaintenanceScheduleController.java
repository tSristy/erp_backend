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
        if (schedule.getRegisteredProduct() == null && schedule.getRegisteredProductId() != null) {
            org.enterprise.crm.service.entity.RegisteredProduct rp = new org.enterprise.crm.service.entity.RegisteredProduct();
            rp.setId(schedule.getRegisteredProductId());
            schedule.setRegisteredProduct(rp);
        }
        return ResponseEntity.ok(service.save(schedule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceSchedule> update(@PathVariable Long id, @RequestBody MaintenanceSchedule schedule) {
        return service.findById(id).map(existing -> {
            existing.setScheduledDate(schedule.getScheduledDate());
            existing.setMaintenanceType(schedule.getMaintenanceType());
            existing.setStatus(schedule.getStatus());
            existing.setNotes(schedule.getNotes());
            if (schedule.getRegisteredProductId() != null) {
                org.enterprise.crm.service.entity.RegisteredProduct rp = new org.enterprise.crm.service.entity.RegisteredProduct();
                rp.setId(schedule.getRegisteredProductId());
                existing.setRegisteredProduct(rp);
            } else if (schedule.getRegisteredProduct() != null) {
                existing.setRegisteredProduct(schedule.getRegisteredProduct());
            } else {
                existing.setRegisteredProduct(null);
            }
            return ResponseEntity.ok(service.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<MaintenanceSchedule> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long companyId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String query,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String search,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        String searchTerm = q != null ? q : (query != null ? query : search);
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.searchMaintenanceSchedules(companyId, searchTerm, pageable);
    }

}