package org.enterprise.config;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.organization.entity.Company;
import org.enterprise.organization.repository.CompanyRepository;
import org.enterprise.security.entity.*;
import org.enterprise.security.repository.*;
import org.enterprise.workflow.entity.WorkflowDefinition;
import org.enterprise.workflow.entity.WorkflowStep;
import org.enterprise.workflow.repository.WorkflowDefinitionRepository;
import org.enterprise.workflow.repository.WorkflowStepRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final PasswordEncoder passwordEncoder;

    // =========================
    // RUN
    // =========================
    @Override
    @Transactional
    public void run(String... args) {

        Company company = seedCompany();

        seedPermissions(); // GLOBAL

        Role adminRole = seedAdminRole(company);

        seedAdminUser(company, adminRole);

        seedAccountUsers(company);

        seedWorkflow(company);

        log.info("Data seeding completed successfully.");
    }

    // =========================
    // COMPANY
    // =========================
    private Company seedCompany() {

        return companyRepository.findByCode("DEFAULT")
                .orElseGet(() -> {

                    Company c = new Company();
                    c.setCode("DEFAULT");
                    c.setName("Default Company");
                    c.setShortName("DEF");
                    c.setEmail("info@default.com");
                    c.setPhone("000000000");
                    c.setMobile("000000000");
                    c.setCountry("Bangladesh");
                    c.setCity("Dhaka");
                    c.setCurrencyCode("BDT");
                    c.setTimezone("Asia/Dhaka");
                    c.setLanguageCode("en");
                    c.setActive(true);
                    c.setStartDate(LocalDate.now());

                    Company saved = companyRepository.save(c);

                    log.info("Company created: {}", saved.getCode());

                    return saved;
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

        createPermission("WORKFLOW_APPROVE", "Workflow Approve");
    }

    private Permission createPermission(String code, String name) {

        return permissionRepository.findByCode(code)
                .orElseGet(() -> {

                    Permission p = new Permission();
                    p.setCode(code);
                    p.setName(name);

                    Permission saved = permissionRepository.save(p);

                    log.info("Permission created: {}", code);

                    return saved;
                });
    }

    // =========================
    // ADMIN ROLE
    // =========================
    private Role seedAdminRole(Company company) {

        return roleRepository.findByCodeAndCompanyId("ADMIN", company.getId())
                .orElseGet(() -> {

                    Role role = new Role();
                    role.setCode("ADMIN");
                    role.setCompanyId(company.getId());

                    role = roleRepository.save(role);

                    List<String> perms = List.of(
                            "INVENTORY_READ",
                            "INVENTORY_WRITE",
                            "FINANCE_READ",
                            "FINANCE_WRITE",
                            "ACCOUNT_VIEW",
                            "ACCOUNT_READ",
                            "ACCOUNT_WRITE",
                            "WORKFLOW_APPROVE"
                    );

                    List<RolePermission> links = new ArrayList<>();

                    for (String code : perms) {

                        Permission permission = permissionRepository.findByCode(code)
                                .orElseThrow();

                        if (!rolePermissionRepository.existsByRoleAndPermission(role, permission)) {

                            RolePermission rp = new RolePermission();
                            rp.setRole(role);
                            rp.setPermission(permission);
                            rp.setCompanyId(company.getId());
                            rp.setAllowed(true);

                            links.add(rolePermissionRepository.save(rp));
                        }
                    }

                    role.setRolePermissions(links);

                    return roleRepository.save(role);
                });
    }

    // =========================
    // USERS
    // =========================
    private void seedAdminUser(Company company, Role role) {

        String username = "admin@" + company.getCode().toLowerCase();

        if (userRepository.findByUsernameAndCompany(username, company.getId()).isPresent())
            return;

        createUser(company, username, role);
    }

    private void seedAccountUsers(Company company) {

        Role viewer = createRole(company, "ACCOUNT_VIEWER", List.of("ACCOUNT_VIEW"));
        Role manager = createRole(company, "ACCOUNT_MANAGER", List.of(
                "ACCOUNT_VIEW",
                "ACCOUNT_READ",
                "ACCOUNT_WRITE"
        ));

        createUser(company, "view_user@" + company.getCode().toLowerCase(), viewer);
        createUser(company, "finance_user@" + company.getCode().toLowerCase(), manager);
    }

    private Role createRole(Company company, String code, List<String> permissions) {

        return roleRepository.findByCodeAndCompanyId(code, company.getId())
                .orElseGet(() -> {

                    Role role = new Role();
                    role.setCode(code);
                    role.setCompanyId(company.getId());

                    role = roleRepository.save(role);

                    List<RolePermission> list = new ArrayList<>();

                    for (String permCode : permissions) {

                        Permission perm = permissionRepository.findByCode(permCode)
                                .orElseThrow();

                        RolePermission rp = new RolePermission();
                        rp.setRole(role);
                        rp.setPermission(perm);
                        rp.setCompanyId(company.getId());
                        rp.setAllowed(true);

                        list.add(rolePermissionRepository.save(rp));
                    }

                    role.setRolePermissions(list);

                    return roleRepository.save(role);
                });
    }

    private void createUser(Company company, String username, Role role) {

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(getPassword()));
        user.setActive(true);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setCompanyId(company.getId());

        user.setRoles(List.of(userRole));

        UserCompany uc = new UserCompany();
        uc.setUser(user);
        uc.setCompany(company);
        uc.setCompanyId(company.getId());
        uc.setDefaultCompany(true);
        uc.setActive(true);

        user.setCompanies(List.of(uc));

        userRepository.save(user);

        log.info("User created: {}", username);
    }

    // =========================
    // WORKFLOW
    // =========================
    private void seedWorkflow(Company company) {

        workflowDefinitionRepository.findByCodeAndCompanyId("PO_APPROVAL", company.getId())
                .orElseGet(() -> {

                    WorkflowDefinition wf = new WorkflowDefinition();
                    wf.setCode("PO_APPROVAL");
                    wf.setName("Purchase Order Approval");
                    wf.setModule("INVENTORY");
                    wf.setActive(true);
                    wf.setCompanyId(company.getId());

                    wf = workflowDefinitionRepository.save(wf);

                    WorkflowStep step = new WorkflowStep();
                    step.setWorkflow(wf);
                    step.setStepNo(1);
                    step.setName("Manager Approval");

                    User admin = userRepository.findByUsernameAndCompany(
                            "admin@" + company.getCode().toLowerCase(),
                            company.getId()
                    ).orElse(null);

                    if (admin != null) {
                        step.setUser(admin);
                    }

                    step.setCompanyId(company.getId());

                    workflowStepRepository.save(step);

                    log.info("Workflow created: PO_APPROVAL");

                    return wf;
                });
    }

    // =========================
    // PASSWORD
    // =========================
    private String getPassword() {
        return System.getenv().getOrDefault("DEFAULT_ADMIN_PASSWORD", "admin123");
    }
}