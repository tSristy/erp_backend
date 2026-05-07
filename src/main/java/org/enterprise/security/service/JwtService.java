package org.enterprise.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.enterprise.security.dto.UserContext;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final String secret = "my-super-secure-256-bit-secret-key-change-this";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UserContext ctx) {

        return Jwts.builder()
                .subject(ctx.getUserId().toString())
                .claim("companyId", ctx.getCompanyId())
                .claim("roles", ctx.getRoles())
                .claim("permissions", ctx.getPermissions())
                .claim("branchIds", ctx.getBranchIds())
                .claim("warehouseIds", ctx.getWarehouseIds())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSigningKey())   // ✅ FIXED
                .compact();
    }


    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}