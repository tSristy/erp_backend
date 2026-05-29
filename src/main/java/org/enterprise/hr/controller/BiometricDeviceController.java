package org.enterprise.hr.controller;

import org.enterprise.hr.dto.BiometricDeviceDto;
import org.enterprise.hr.service.BiometricDeviceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/biometric-devices")
@RequiredArgsConstructor
public class BiometricDeviceController {

    private final BiometricDeviceService service;

    @PostMapping
    public ResponseEntity<BiometricDeviceDto> create(@RequestBody BiometricDeviceDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BiometricDeviceDto> update(@PathVariable Long id, @RequestBody BiometricDeviceDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BiometricDeviceDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<BiometricDeviceDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
