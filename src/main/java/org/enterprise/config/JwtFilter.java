package org.enterprise.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.security.service.JwtService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Skip if no token
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        Claims claims;
        try {

            // =========================
            // CHECK TOKEN BLACKLIST
            // =========================

            try {
                String blacklistKey = "BLACKLIST:TOKEN:" + token;
                Boolean blacklisted = redisTemplate.hasKey(blacklistKey);

                if (Boolean.TRUE.equals(blacklisted)) {
                    unauthorized(response, "Token has been revoked");
                    return;
                }
            } catch (Exception redisEx) {
                System.err.println("WARNING: Could not connect to Redis to check token blacklist. Proceeding without blacklist check.");
            }

            // =========================
            // PARSE JWT
            // =========================

            claims = jwtService.extractClaims(token);

        } catch (Exception ex) {
            ex.printStackTrace();
            unauthorized(response, "Invalid or expired token. Error: " + ex.getMessage());
            return;
        }

        if ("PRE_AUTH".equals(claims.get("type"))) {
            try {
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
                SecurityContextHolder.clearContext();
            }
            return;
        }

        try {

            // =========================
            // BUILD TENANT CONTEXT
            // =========================

            TenantContext context = new TenantContext();

            context.setUserId(
                    Long.parseLong(claims.getSubject())
            );

            context.setCompanyId(
                    Long.valueOf(claims.get("companyId").toString())
            );

            context.setRoles(
                    convertStringList(claims.get("roles"))
            );

            context.setPermissions(
                    convertStringList(claims.get("permissions"))
            );

            context.setBranchIds(
                    convertLongList(claims.get("branchIds"))
            );

            context.setWarehouseIds(
                    convertLongList(claims.get("warehouseIds"))
            );

            context.setProfitCenterIds(
                    convertLongList(claims.get("profitCenterIds"))
            );

            context.setCostCenterIds(
                    convertLongList(claims.get("costCenterIds"))
            );

            TenantContext.set(context);

            // =========================
            // SPRING SECURITY AUTH
            // =========================

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            context.getUserId(),
                            null,
                            context.getPermissions()
                                    .stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .toList()
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (Exception ex) {
            ex.printStackTrace();
            unauthorized(response, "Invalid or expired token. Error: " + ex.getMessage());
            return;

        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {

            TenantContext.clear();

            SecurityContextHolder.clearContext();
        }
    }

    // =========================
    // HELPERS
    // =========================

    private List<String> convertStringList(Object obj) {

        if (obj == null) {
            return List.of();
        }

        return ((List<?>) obj)
                .stream()
                .map(String::valueOf)
                .toList();
    }

    private List<Long> convertLongList(Object obj) {

        if (obj == null) {
            return List.of();
        }

        return ((List<?>) obj)
                .stream()
                .map(x -> Long.valueOf(x.toString()))
                .toList();
    }

    private void unauthorized(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType("application/json");

        response.getWriter().write("""
            {
              "status": 401,
              "message": "%s"
            }
            """.formatted(message));
    }
}