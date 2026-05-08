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

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;

    public String login(LoginRequest request) {

        // =========================================
        // 1. Resolve Company
        // =========================================

        Company company = companyRepository
                .findByCode(request.getCompanyCode())
                .orElseThrow(() -> new RuntimeException("Invalid company"));

        // =========================================
        // 2. Load User + Roles
        // =========================================

        User user = userRepository
                .findWithRolesByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // =========================================
        // 3. Validate Password
        // =========================================

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new RuntimeException("Invalid credentials");
        }

        // =========================================
        // 4. Validate User Active
        // =========================================

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new RuntimeException("User account disabled");
        }

        // =========================================
        // 5. Validate Company Membership
        // =========================================

        UserCompany membership = user.getCompanies()
                .stream()
                .filter(UserCompany::getActive)
                .filter(uc -> uc.getCompany().getId().equals(company.getId()))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("User not assigned to company"));

        // =========================================
        // 6. Extract Tenant Roles
        // =========================================

        List<String> roles = user.getRoles()
                .stream()
                .filter(UserRole::getActive)
                .filter(ur ->
                        ur.getCompanyId().equals(company.getId()))
                .map(ur -> ur.getRole().getCode())
                .distinct()
                .toList();

        // =========================================
        // 7. Extract Permissions
        // =========================================

        List<String> permissions = user.getRoles()
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

        // =========================================
        // 8. Branch Scope
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
        // 9. Warehouse Scope
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
        // 10. Profit Center Scope
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
        // 11. Cost Center Scope
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
        // 12. Build Context
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
        // 13. Generate JWT
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

        redisTemplate.opsForValue().set(
                key,
                "LOGGED_OUT",
                ttl,
                TimeUnit.SECONDS
        );
    }
}