package org.enterprise.hr.controller;

import org.enterprise.hr.dto.PayrollProcessDto;
import org.enterprise.hr.dto.PayslipDto;
import org.enterprise.hr.service.PayrollProcessService;
import org.enterprise.hr.service.PayslipService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/hr/payroll-processs")
@RequiredArgsConstructor
public class PayrollProcessController {

    private final PayrollProcessService service;
    private final PayslipService payslipService;

    @PostMapping
    public ResponseEntity<PayrollProcessDto> create(@Valid @RequestBody PayrollProcessDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayrollProcessDto> update(@PathVariable Long id, @Valid @RequestBody PayrollProcessDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollProcessDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PayrollProcessDto>> search(Pageable pageable) {
        return ResponseEntity.ok(service.search(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Payslip Endpoints
    @PostMapping("/payslips")
    public ResponseEntity<PayslipDto> createPayslip(@Valid @RequestBody PayslipDto dto) {
        return ResponseEntity.ok(payslipService.create(dto));
    }

    @PutMapping("/payslips/{id}")
    public ResponseEntity<PayslipDto> updatePayslip(@PathVariable Long id, @Valid @RequestBody PayslipDto dto) {
        return ResponseEntity.ok(payslipService.update(id, dto));
    }

    @GetMapping("/payslips/{id}")
    public ResponseEntity<PayslipDto> getPayslipById(@PathVariable Long id) {
        return ResponseEntity.ok(payslipService.getById(id));
    }

    @GetMapping("/payslips")
    public ResponseEntity<Page<PayslipDto>> searchPayslips(Pageable pageable) {
        return ResponseEntity.ok(payslipService.search(pageable));
    }

    @DeleteMapping("/payslips/{id}")
    public ResponseEntity<Void> deletePayslip(@PathVariable Long id) {
        payslipService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.hr.dto.PayrollProcessDto> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.searchPayrollProcesss(org.enterprise.common.util.TenantContext.getCompanyId(), q, pageable);
    }

}