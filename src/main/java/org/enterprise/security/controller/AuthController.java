package org.enterprise.security.controller;

import lombok.RequiredArgsConstructor;
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

    // 🔐 LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        String token = authService.login(request);

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