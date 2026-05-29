package org.enterprise.hr.controller;

import org.enterprise.hr.dto.BiometricRawLogDto;
import org.enterprise.hr.service.BiometricRawLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/biometric-raw-logs")
@RequiredArgsConstructor
public class BiometricRawLogController {

    private final BiometricRawLogService service;

    @PostMapping
    public ResponseEntity<BiometricRawLogDto> create(@RequestBody BiometricRawLogDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BiometricRawLogDto> update(@PathVariable Long id, @RequestBody BiometricRawLogDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BiometricRawLogDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<BiometricRawLogDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
