package org.enterprise.hr.controller;

import org.enterprise.hr.dto.PayslipComponentDto;
import org.enterprise.hr.service.PayslipComponentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/payslip-components")
@RequiredArgsConstructor
public class PayslipComponentController {

    private final PayslipComponentService service;

    @PostMapping
    public ResponseEntity<PayslipComponentDto> create(@RequestBody PayslipComponentDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayslipComponentDto> update(@PathVariable Long id, @RequestBody PayslipComponentDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayslipComponentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PayslipComponentDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
