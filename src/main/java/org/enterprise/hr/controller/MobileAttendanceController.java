package org.enterprise.hr.controller;

import org.enterprise.hr.dto.MobileAttendanceDto;
import org.enterprise.hr.service.MobileAttendanceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/mobile-attendances")
@RequiredArgsConstructor
public class MobileAttendanceController {

    private final MobileAttendanceService service;

    @PostMapping
    public ResponseEntity<MobileAttendanceDto> create(@RequestBody MobileAttendanceDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MobileAttendanceDto> update(@PathVariable Long id, @RequestBody MobileAttendanceDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MobileAttendanceDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<MobileAttendanceDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
