package org.enterprise.security.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.security.dto.CompanyContextRequest;
import org.enterprise.security.dto.LoginRequest;
import org.enterprise.security.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 🔐 LOGIN (PRE-AUTH)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {

        Map<String, Object> result = authService.preAuthenticate(request, httpRequest);

        return ResponseEntity.ok(result);
    }

    // 🏢 CONTEXT BUILDER
    @PostMapping("/context")
    public ResponseEntity<?> buildContext(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CompanyContextRequest request) {

        String token = authService.buildUserContext(authHeader, request.getCompanyCode());

        return ResponseEntity.ok(
                Map.of(
                        "accessToken", token,
                        "tokenType", "Bearer"
                )
        );
    }

    // 🔄 SWITCH COMPANY
    @PostMapping("/switch-company")
    public ResponseEntity<?> switchCompany(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CompanyContextRequest request) {

        String token = authService.switchCompany(authHeader, request.getCompanyCode());

        return ResponseEntity.ok(
                Map.of(
                        "accessToken", token,
                        "tokenType", "Bearer"
                )
        );
    }

    // 🚪 LOGOUT (fixed)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader
    ) {

        authService.logout(authHeader);

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully")
        );
    }
}