package org.enterprise.security.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.dto.PaginatedResponse;
import org.enterprise.security.dto.LoginAuditDto;
import org.enterprise.security.service.LoginAuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/security/login-audit")
@RequiredArgsConstructor
public class LoginAuditController {

    private final LoginAuditService loginAuditService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<LoginAuditDto>> getLoginAudits(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        
        return ResponseEntity.ok(loginAuditService.getLoginAudits(page, limit, search));
    }
}
