package org.enterprise.security.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.organization.entity.Company;
import org.enterprise.organization.repository.CompanyRepository;
import org.enterprise.security.dto.LoginRequest;
import org.enterprise.security.dto.UserContext;
import org.enterprise.security.entity.User;
import org.enterprise.security.entity.UserCompany;
import org.enterprise.security.entity.UserRole;
import org.enterprise.security.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final org.enterprise.security.repository.PermissionRepository permissionRepository;
    private final org.enterprise.security.repository.LoginAuditRepository loginAuditRepository;

    public java.util.Map<String, Object> preAuthenticate(LoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            // =========================================
            // 1. Load User + Roles
            // =========================================
            User user = userRepository
                    .findWithRolesByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            // =========================================
            // 2. Validate Password
            // =========================================
            if (!passwordEncoder.matches(
                    request.getPassword(),
                    user.getPassword()
            )) {
                throw new RuntimeException("Invalid credentials");
            }

            // =========================================
            // 3. Validate User Active
            // =========================================
            if (Boolean.FALSE.equals(user.getActive())) {
                throw new RuntimeException("User account disabled");
            }

            // =========================================
            // 4. Get Authorized Companies
            // =========================================
            boolean isSuperadmin = user.getRoles() != null && user.getRoles().stream()
                    .anyMatch(ur -> "SUPER-ADMIN".equals(ur.getRole().getCode()) && Boolean.TRUE.equals(ur.getActive()));

            List<String> companyCodes;
            if (isSuperadmin) {
                companyCodes = companyRepository.findAll().stream()
                        .map(Company::getCode)
                        .toList();
            } else {
                companyCodes = user.getCompanies()
                        .stream()
                        .filter(UserCompany::getActive)
                        .map(uc -> uc.getCompany().getCode())
                        .toList();
            }

            // =========================================
            // 5. Generate Pre-Auth Token
            // =========================================
            String token = jwtService.generatePreAuthToken(user.getUsername());

            logLoginAudit(request.getUsername(), httpRequest, true, null);

            return java.util.Map.of(
                    "preAuthToken", token,
                    "companyCodes", companyCodes
            );
        } catch (Exception e) {
            logLoginAudit(request.getUsername(), httpRequest, false, e.getMessage());
            throw e;
        }
    }

    private void logLoginAudit(String username, jakarta.servlet.http.HttpServletRequest httpRequest, boolean success, String failureReason) {
        org.enterprise.security.entity.LoginAudit audit = new org.enterprise.security.entity.LoginAudit();
        audit.setUsername(username);
        if (httpRequest != null) {
            String ip = httpRequest.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) ip = httpRequest.getRemoteAddr();
            audit.setIpAddress(ip);
            audit.setDeviceInfo(httpRequest.getHeader("User-Agent"));
        }
        audit.setSuccess(success);
        audit.setLoginAt(java.time.LocalDateTime.now());
        audit.setFailureReason(failureReason);
        
        Long companyId = 1L;
        try {
            User user = userRepository.findWithRolesByUsername(username).orElse(null);
            if (user != null && user.getCompanies() != null && !user.getCompanies().isEmpty()) {
                companyId = user.getCompanies().stream()
                        .filter(org.enterprise.security.entity.UserCompany::getActive)
                        .findFirst()
                        .map(uc -> uc.getCompany().getId())
                        .orElse(1L);
            }
        } catch (Exception ignored) {}
        audit.setCompanyId(companyId);
        
        loginAuditRepository.save(audit);
    }


    public String buildUserContext(String preAuthToken, String companyCode) {

        // =========================================
        // 1. Validate Pre-Auth Token
        // =========================================
        if (preAuthToken == null || !preAuthToken.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token format");
        }
        String token = preAuthToken.substring(7);

        io.jsonwebtoken.Claims claims = jwtService.extractClaims(token);
        if (!"PRE_AUTH".equals(claims.get("type"))) {
            throw new RuntimeException("Invalid token type");
        }

        String username = claims.getSubject();
        return generateContextToken(username, companyCode);
    }

    public String switchCompany(String authHeader, String companyCode) {
        
        // =========================================
        // 1. Validate Access Token
        // =========================================
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token format");
        }
        String token = authHeader.substring(7);

        io.jsonwebtoken.Claims claims = jwtService.extractClaims(token);
        if ("PRE_AUTH".equals(claims.get("type"))) {
            throw new RuntimeException("Cannot switch company with a pre-auth token");
        }

        Long userId = Long.parseLong(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return generateContextToken(user.getUsername(), companyCode);
    }

    private String generateContextToken(String username, String companyCode) {
        
        // =========================================
        // 1. Resolve Company
        // =========================================
        Company company = companyRepository
                .findByCode(companyCode)
                .orElseThrow(() -> new RuntimeException("Invalid company"));

        // =========================================
        // 2. Load User + Roles
        // =========================================
        User user = userRepository
                .findWithRolesByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new RuntimeException("User account disabled");
        }

        // =========================================
        // 3. Validate Company Membership
        // =========================================
        boolean isSuperadmin = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(ur -> "SUPER-ADMIN".equals(ur.getRole().getCode()) && Boolean.TRUE.equals(ur.getActive()));

        if (!isSuperadmin) {
            user.getCompanies()
                    .stream()
                    .filter(UserCompany::getActive)
                    .filter(uc -> uc.getCompany().getId().equals(company.getId()))
                    .findFirst()
                    .orElseThrow(() ->
                            new RuntimeException("User not assigned to company"));
        }

        // =========================================
        // 4. Extract Tenant Roles
        // =========================================
        List<String> roles;
        if (isSuperadmin) {
            roles = List.of("SUPER-ADMIN");
        } else {
            roles = user.getRoles()
                    .stream()
                    .filter(UserRole::getActive)
                    .filter(ur ->
                            ur.getCompanyId().equals(company.getId()))
                    .map(ur -> ur.getRole().getCode())
                    .distinct()
                    .toList();
        }

        // =========================================
        // 5. Extract Permissions
        // =========================================
        List<String> permissions;
        if (isSuperadmin) {
            permissions = permissionRepository.findAll().stream()
                    .map(org.enterprise.security.entity.Permission::getCode)
                    .toList();
        } else {
            permissions = user.getRoles()
                    .stream()
                    .filter(UserRole::getActive)
                    .filter(ur ->
                            ur.getCompanyId().equals(company.getId()))
                    .flatMap(ur ->
                            ur.getRole()
                                    .getRolePermissions()
                                    .stream())
                    .filter(rp -> Boolean.TRUE.equals(rp.getAllowed()))
                    .map(rp -> rp.getPermission().getCode())
                    .distinct()
                    .toList();
        }

        // =========================================
        // 6. Branch Scope
        // =========================================
        List<Long> branchIds = user.getUserBranches() != null
                ? user.getUserBranches()
                .stream()
                .filter(ub ->
                        ub.getCompanyId().equals(company.getId()))
                .map(ub -> ub.getBranch().getId())
                .distinct()
                .toList()
                : List.of();

        // =========================================
        // 7. Warehouse Scope
        // =========================================
        List<Long> warehouseIds = user.getUserWarehouses() != null
                ? user.getUserWarehouses()
                .stream()
                .filter(uw ->
                        uw.getCompanyId().equals(company.getId()))
                .map(uw -> uw.getWarehouse().getId())
                .distinct()
                .toList()
                : List.of();

        // =========================================
        // 8. Profit Center Scope
        // =========================================
        List<Long> profitCenterIds = user.getUserProfitCenters() != null
                ? user.getUserProfitCenters()
                .stream()
                .filter(up ->
                        up.getCompanyId().equals(company.getId()))
                .map(up -> up.getProfitCenter().getId())
                .distinct()
                .toList()
                : List.of();

        // =========================================
        // 9. Cost Center Scope
        // =========================================
        List<Long> costCenterIds = user.getUserCostCenters() != null
                ? user.getUserCostCenters()
                .stream()
                .filter(uc ->
                        uc.getCompanyId().equals(company.getId()))
                .map(uc -> uc.getCostCenter().getId())
                .distinct()
                .toList()
                : List.of();

        // =========================================
        // 10. Build Context
        // =========================================
        UserContext context = new UserContext(
                user.getId(),
                company.getId(),
                roles,
                permissions,
                branchIds,
                warehouseIds,
                profitCenterIds,
                costCenterIds,
                company.getTimezone(),
                company.getLanguageCode()
        );

        // =========================================
        // 11. Generate JWT
        // =========================================
        return jwtService.generateToken(context);
    }


    // =========================================
    // LOGOUT
    // =========================================

    public void logout(String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authHeader.substring(7);

        long ttl = 24 * 60 * 60;

        String key = "BLACKLIST:TOKEN:" + token;

        try {
            redisTemplate.opsForValue().set(
                    key,
                    "LOGGED_OUT",
                    ttl,
                    TimeUnit.SECONDS
            );
        } catch (Exception ex) {
            System.err.println("WARNING: Could not connect to Redis to blacklist token on logout. Error: " + ex.getMessage());
        }
    }
}