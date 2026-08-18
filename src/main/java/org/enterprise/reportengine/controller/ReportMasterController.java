package org.enterprise.reportengine.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.reportengine.entity.ReportMaster;
import org.enterprise.reportengine.service.ReportMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report-engine/report-master")
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
    public ResponseEntity<ReportMaster> create(@RequestBody ReportMaster reportMaster) {
        return ResponseEntity.ok(reportMasterService.save(reportMaster));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORT_ENGINE_WRITE')")
    public ResponseEntity<ReportMaster> update(@PathVariable Long id, @RequestBody ReportMaster reportMaster) {
        reportMaster.setId(id);
        return ResponseEntity.ok(reportMasterService.save(reportMaster));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORT_ENGINE_WRITE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reportMasterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
