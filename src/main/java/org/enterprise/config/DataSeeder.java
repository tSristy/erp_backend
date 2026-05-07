package org.enterprise.config;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.enterprise.organization.entity.Company;
import org.enterprise.organization.repository.CompanyRepository;
import org.enterprise.security.entity.*;
import org.enterprise.security.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.enterprise.workflow.entity.WorkflowDefinition;
import org.enterprise.workflow.repository.WorkflowDefinitionRepository;
import org.enterprise.workflow.entity.WorkflowStep;
import org.enterprise.workflow.repository.WorkflowStepRepository;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowStepRepository workflowStepRepository;

    @Override
    @Transactional
    public void run(String... args) {

        Company company = seedCompany();

        seedPermissions();          // GLOBAL
        Role adminRole = seedRoles(company);
        seedAdminUser(company, adminRole);
        
        seedAccountUsers(company);
        seedWorkflowDefinitions(company, adminRole);
    }

    // =========================
    // COMPANY
    // =========================
    private Company seedCompany() {

        return companyRepository.findByCode("DEFAULT")
                .orElseGet(() -> {
                    Company company = new Company();
                    company.setCode("DEFAULT");
                    company.setName("Default Company");
                    company.setShortName("DEF");

                    company.setEmail("info@default.com");
                    company.setPhone("000000000");
                    company.setMobile("000000000");

                    company.setCountry("Bangladesh");
                    company.setCity("Dhaka");

                    company.setCurrencyCode("BDT");
                    company.setTimezone("Asia/Dhaka");
                    company.setLanguageCode("en");

                    company.setActive(true);
                    company.setStartDate(LocalDate.now());

                    return companyRepository.save(company);
                });
    }

    // =========================
    // PERMISSIONS (GLOBAL)
    // =========================
    private void seedPermissions() {

        createPermission("INVENTORY_READ", "Inventory Read");
        createPermission("INVENTORY_WRITE", "Inventory Write");
        createPermission("FINANCE_READ", "Finance Read");
        createPermission("FINANCE_WRITE", "Finance Write");
        
        createPermission("ACCOUNT_VIEW", "Account View");
        createPermission("ACCOUNT_READ", "Account Read");
        createPermission("ACCOUNT_WRITE", "Account Write");
    }

    private Permission createPermission(String code, String name) {

        return permissionRepository.findByCode(code)
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode(code);
                    p.setName(name);
                    return permissionRepository.save(p);
                });
    }

    // =========================
    // ROLES
    // =========================
    private Role seedRoles(Company company) {

        String roleCode = "ADMIN_" + company.getId();

        return roleRepository.findByCodeAndCompanyId(roleCode, company.getId())
                .orElseGet(() -> {

                    Role role = new Role();
                    role.setCode(roleCode);
                    role.setCompanyId(company.getId());

                    role = roleRepository.save(role);

                    List<RolePermission> rolePermissions = new ArrayList<>();

                    rolePermissions.add(createRolePermission(role, "INVENTORY_READ"));
                    rolePermissions.add(createRolePermission(role, "INVENTORY_WRITE"));
                    rolePermissions.add(createRolePermission(role, "FINANCE_READ"));
                    rolePermissions.add(createRolePermission(role, "FINANCE_WRITE"));

                    role.setRolePermissions(rolePermissions);

                    return roleRepository.save(role);
                });
    }

    private RolePermission createRolePermission(Role role, String permissionCode) {

        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionCode));

        RolePermission rp = new RolePermission();
        rp.setRole(role);
        rp.setPermission(permission);
        rp.setCompanyId(role.getCompanyId());
        rp.setAllowed(true);

        return rp;
    }

    // =========================
    // ADMIN USER
    // =========================
    private void seedAdminUser(Company company, Role adminRole) {

        String username = "admin@" + company.getCode();

        if (userRepository.findByUsernameAndCompanyId(username, company.getId()).isPresent())
            return;

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(getDefaultPassword()));
        user.setActive(true);
        user.setCompanyId(company.getId());

        // USER ROLE
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(adminRole);
        userRole.setCompanyId(company.getId());

        user.setRoles(List.of(userRole));

        // USER COMPANY
        UserCompany userCompany = new UserCompany();
        userCompany.setUser(user);
        userCompany.setCompany(company);
        userCompany.setDefaultCompany(true);
        userCompany.setActive(true);

        user.setCompanies(List.of(userCompany));

        userRepository.save(user);
    }
    
    // =========================
    // ACCOUNT USERS
    // =========================
    private void seedAccountUsers(Company company) {
        // Viewer Role
        Role viewerRole = roleRepository.findByCodeAndCompanyId("ACCOUNT_VIEWER", company.getId())
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setCode("ACCOUNT_VIEWER");
                    role.setCompanyId(company.getId());
                    role = roleRepository.save(role);

                    List<RolePermission> perms = new ArrayList<>();
                    perms.add(createRolePermission(role, "ACCOUNT_VIEW"));
                    role.setRolePermissions(perms);
                    return roleRepository.save(role);
                });

        // Manager Role
        Role managerRole = roleRepository.findByCodeAndCompanyId("ACCOUNT_MANAGER", company.getId())
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setCode("ACCOUNT_MANAGER");
                    role.setCompanyId(company.getId());
                    role = roleRepository.save(role);

                    List<RolePermission> perms = new ArrayList<>();
                    perms.add(createRolePermission(role, "ACCOUNT_VIEW"));
                    perms.add(createRolePermission(role, "ACCOUNT_READ"));
                    perms.add(createRolePermission(role, "ACCOUNT_WRITE"));
                    role.setRolePermissions(perms);
                    return roleRepository.save(role);
                });

        // view_user
        seedCustomUser(company, "view_user@" + company.getCode().toLowerCase() + ".com", viewerRole);
        // finance_user
        seedCustomUser(company, "finance_user@" + company.getCode().toLowerCase() + ".com", managerRole);
    }
    
    private void seedCustomUser(Company company, String username, Role role) {
        if (userRepository.findByUsernameAndCompanyId(username, company.getId()).isPresent())
            return;

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(getDefaultPassword()));
        user.setActive(true);
        user.setCompanyId(company.getId());

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setCompanyId(company.getId());

        user.setRoles(List.of(userRole));

        UserCompany userCompany = new UserCompany();
        userCompany.setUser(user);
        userCompany.setCompany(company);
        userCompany.setDefaultCompany(true);
        userCompany.setActive(true);

        user.setCompanies(List.of(userCompany));

        userRepository.save(user);
    }
    
    // =========================
    // WORKFLOW DEFINITIONS
    // =========================
    private void seedWorkflowDefinitions(Company company, Role adminRole) {
        workflowDefinitionRepository.findByCode("PO_APPROVAL").orElseGet(() -> {
            WorkflowDefinition def = new WorkflowDefinition();
            def.setCode("PO_APPROVAL");
            def.setName("Purchase Order Approval Workflow");
            def.setModule("INVENTORY");
            def.setActive(true);
            def = workflowDefinitionRepository.save(def);

            // Fetch admin user
            User admin = userRepository.findByUsernameAndCompanyId("admin@" + company.getCode(), company.getId()).orElse(null);

            if (admin != null) {
                WorkflowStep step1 = new WorkflowStep();
                step1.setWorkflow(def);
                step1.setStepNo(1);
                step1.setName("Manager Approval");
                step1.setUser(admin);
                workflowStepRepository.save(step1);
            }

            return def;
        });
    }

    private String getDefaultPassword() {
        return System.getenv().getOrDefault("DEFAULT_ADMIN_PASSWORD", "admin123");
    }
}