package org.enterprise.hr.controller;

import org.enterprise.hr.dto.PayrollProcessDto;
import org.enterprise.hr.dto.PayslipDto;
import org.enterprise.hr.dto.PayslipComponentDto;
import org.enterprise.hr.service.PayrollProcessService;
import org.enterprise.hr.service.PayslipService;
import org.enterprise.hr.service.PayslipComponentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hr/payroll-processs")
@RequiredArgsConstructor
public class PayrollProcessController {

    private final PayrollProcessService service;
    private final PayslipService payslipService;
    private final PayslipComponentService payslipComponentService;

    @PostMapping
    public ResponseEntity<PayrollProcessDto> create(@RequestBody PayrollProcessDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayrollProcessDto> update(@PathVariable Long id, @RequestBody PayrollProcessDto dto) {
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
    public ResponseEntity<PayslipDto> createPayslip(@RequestBody PayslipDto dto) {
        return ResponseEntity.ok(payslipService.create(dto));
    }

    @PutMapping("/payslips/{id}")
    public ResponseEntity<PayslipDto> updatePayslip(@PathVariable Long id, @RequestBody PayslipDto dto) {
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

    // PayslipComponent Endpoints
    @PostMapping("/payslip-components")
    public ResponseEntity<PayslipComponentDto> createPayslipComponent(@RequestBody PayslipComponentDto dto) {
        return ResponseEntity.ok(payslipComponentService.create(dto));
    }

    @PutMapping("/payslip-components/{id}")
    public ResponseEntity<PayslipComponentDto> updatePayslipComponent(@PathVariable Long id, @RequestBody PayslipComponentDto dto) {
        return ResponseEntity.ok(payslipComponentService.update(id, dto));
    }

    @GetMapping("/payslip-components/{id}")
    public ResponseEntity<PayslipComponentDto> getPayslipComponentById(@PathVariable Long id) {
        return ResponseEntity.ok(payslipComponentService.getById(id));
    }

    @GetMapping("/payslip-components")
    public ResponseEntity<Page<PayslipComponentDto>> searchPayslipComponents(Pageable pageable) {
        return ResponseEntity.ok(payslipComponentService.search(pageable));
    }

    @DeleteMapping("/payslip-components/{id}")
    public ResponseEntity<Void> deletePayslipComponent(@PathVariable Long id) {
        payslipComponentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
