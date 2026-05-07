package org.enterprise.security.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.security.dto.LoginRequest;
import org.enterprise.security.dto.UserContext;
import org.enterprise.security.entity.User;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;

    public String login(LoginRequest request) {

        // 1. Load user
        User user = userRepository.findByUsernameWithRole(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // 2. Password validation
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 3. Active check
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new RuntimeException("User account disabled");
        }

        // 4. Extract ACTIVE roles
        List<String> roles = user.getRoles()
                .stream()
                .filter(UserRole::getActive)
                .map(ur -> ur.getRole().getCode())
                .distinct()
                .toList();

        // 5. Extract permissions
        List<String> permissions = user.getRoles()
                .stream()
                .filter(UserRole::getActive)
                .flatMap(ur -> ur.getRole()
                        .getRolePermissions()
                        .stream())
                .filter(rp -> Boolean.TRUE.equals(rp.getAllowed()))
                .map(rp -> rp.getPermission().getCode())
                .distinct()
                .toList();

        // 6. Branch scope
        List<Long> branchIds = user.getUserBranches() != null
                ? user.getUserBranches()
                .stream()
                .map(ub -> ub.getBranch().getId())
                .distinct()
                .toList()
                : List.of();

        // 7. Warehouse scope
        List<Long> warehouseIds = user.getUserWarehouses() != null
                ? user.getUserWarehouses()
                .stream()
                .map(uw -> uw.getWarehouse().getId())
                .distinct()
                .toList()
                : List.of();

        // 8. Profit Center scope
        List<Long> profitCenterIds = user.getUserProfitCenters() != null
                ? user.getUserProfitCenters()
                .stream()
                .map(up -> up.getProfitCenter().getId())
                .distinct()
                .toList()
                : List.of();

        // 9. Cost Center scope
        List<Long> costCenterIds = user.getUserCostCenters() != null
                ? user.getUserCostCenters()
                .stream()
                .map(uc -> uc.getCostCenter().getId())
                .distinct()
                .toList()
                : List.of();

        // 10. Build UserContext
        UserContext context = new UserContext(
                user.getId(),
                user.getCompanyId(),
                roles,
                permissions,
                branchIds,
                warehouseIds,
                profitCenterIds,
                costCenterIds,
                "UTC",
                "en"
        );

        // 11. Generate JWT
        return jwtService.generateToken(context);
    }

    /// 🚪 LOGOUT (ENTERPRISE VERSION)
    public void logout(String authHeader) {

        // 1. Validate header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        // 2. Extract token
        String token = authHeader.substring(7);

        // 3. Calculate TTL (optional but important)
        // If JWT expires in 24h, keep blacklist for same duration
        long ttl = 24 * 60 * 60; // seconds

        // 4. Store token in blacklist
        String key = "BLACKLIST:TOKEN:" + token;

        redisTemplate.opsForValue().set(
                key,
                "LOGGED_OUT",
                ttl,
                TimeUnit.SECONDS
        );

        // 5. Optional: also store user session revoke info
        // redisTemplate.opsForSet().add("LOGGED_OUT_USERS", userId);
    }
}