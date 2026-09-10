package org.enterprise.security.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.entity.SystemAuditLog;
import org.enterprise.common.repository.SystemAuditLogRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/iam/systemaudit")
@RequiredArgsConstructor
public class SystemAuditLogController {

    private final SystemAuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<List<SystemAuditLog>> getAllAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "changedAt")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SystemAuditLog> getAuditLogById(@PathVariable Long id) {
        return auditLogRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
