package org.enterprise.common.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.security.entity.Permission;
import org.enterprise.security.entity.Role;
import org.enterprise.security.entity.RolePermission;
import org.enterprise.security.repository.PermissionRepository;
import org.enterprise.security.repository.RolePermissionRepository;
import org.enterprise.security.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ReportEnginePermissionSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Running Report Engine Permission Seeder...");

        String[] perms = {"REPORT_ENGINE_VIEW", "REPORT_ENGINE_READ", "REPORT_ENGINE_WRITE"};
        String[] permNames = {"Report Engine View", "Report Engine Read", "Report Engine Write"};

        for (int i = 0; i < perms.length; i++) {
            String code = perms[i];
            String name = permNames[i];
            if (permissionRepository.findByCode(code).isEmpty()) {
                Permission p = new Permission();
                p.setCode(code);
                p.setName(name);
                permissionRepository.save(p);
            }
        }

        List<Role> roles = roleRepository.findAll();
        for (Role role : roles) {
            if ("ADMIN".equals(role.getCode()) || "SUPER-ADMIN".equals(role.getCode())) {
                for (String pCode : perms) {
                    Permission perm = permissionRepository.findByCode(pCode).orElse(null);
                    if (perm != null) {
                        boolean exists = rolePermissionRepository.findAll().stream()
                            .anyMatch(rp -> rp.getRole().getId().equals(role.getId()) && rp.getPermission().getCode().equals(pCode));
                        
                        if (!exists) {
                            RolePermission rp = new RolePermission();
                            rp.setRole(role);
                            rp.setPermission(perm);
                            rp.setCompanyId(role.getCompanyId());
                            rolePermissionRepository.save(rp);
                            log.info("Granted {} to role {} (company {})", pCode, role.getCode(), role.getCompanyId());
                        }
                    }
                }
            }
        }
        log.info("Report Engine permissions seeded successfully.");
    }
}
