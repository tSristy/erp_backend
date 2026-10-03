package org.enterprise.reportengine.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.reportengine.entity.ReportMaster;
import org.enterprise.reportengine.service.ReportMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/report-engine/report-master")
@RequiredArgsConstructor
public class ReportMasterController {

    private final ReportMasterService reportMasterService;

    @GetMapping
    @PreAuthorize("hasAuthority('REPORT_ENGINE_VIEW') or hasAuthority('REPORT_ENGINE_READ') or hasAuthority('REPORT_ENGINE_WRITE')")
    public ResponseEntity<List<ReportMaster>> getAll() {
        return ResponseEntity.ok(reportMasterService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORT_ENGINE_VIEW') or hasAuthority('REPORT_ENGINE_READ') or hasAuthority('REPORT_ENGINE_WRITE')")
    public ResponseEntity<ReportMaster> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reportMasterService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('REPORT_ENGINE_WRITE')")
    public ResponseEntity<ReportMaster> create(@Valid @RequestBody ReportMaster reportMaster) {
        ReportMaster created = reportMasterService.save(reportMaster);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\'REPORT_ENGINE_WRITE\')")
    public ResponseEntity<ReportMaster> update(@PathVariable Long id, @Valid @RequestBody ReportMaster reportMaster) {
        reportMaster.setId(id);
        return ResponseEntity.ok(reportMasterService.save(reportMaster));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORT_ENGINE_WRITE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reportMasterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<ReportMaster> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return reportMasterService.searchReportMasters(org.enterprise.common.util.TenantContext.getCompanyId(), q, pageable);
    }

}