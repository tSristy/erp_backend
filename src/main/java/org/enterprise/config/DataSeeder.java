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
import java.util.Set;
import java.util.HashSet;

@Slf4j
@Component
@Profile({"default", "dev", "local"})
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
    private final ModuleRepository moduleRepository;
    private final org.enterprise.security.repository.MenuRepository menuRepository;

    // =========================
    // RUN
    // =========================
    @Override
    @Transactional
    public void run(String... args) {

        Company company = seedCompany();
        Company acme = seedAcmeCompany();

        seedPermissions(); // GLOBAL

        Role adminRole = seedAdminRole(company);

        seedAdminUser(company, adminRole);

        seedSuperAdmin(company);

        seedAccountUsers(company);

        seedWorkflow(company);
        
        seedModules(company);
        seedModules(acme);

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

    private Company seedAcmeCompany() {
        return companyRepository.findByCode("ACME")
                .orElseGet(() -> {
                    Company c = new Company();
                    c.setCode("ACME");
                    c.setName("ACME Corp");
                    c.setShortName("ACME");
                    c.setEmail("info@acme.com");
                    c.setPhone("111111111");
                    c.setMobile("111111111");
                    c.setCountry("USA");
                    c.setCity("New York");
                    c.setCurrencyCode("USD");
                    c.setTimezone("America/New_York");
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

                    Set<RolePermission> links = new HashSet<>();

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

    private void seedSuperAdmin(Company company) {
        Role superAdminRole = roleRepository.findByCodeAndCompanyId("SUPER-ADMIN", company.getId())
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setCode("SUPER-ADMIN");
                    role.setCompanyId(company.getId());
                    return roleRepository.save(role);
                });

        if (userRepository.findByUsername("superadmin@default").isEmpty()) {
            User user = new User();
            user.setUsername("superadmin@default");
            user.setPassword(passwordEncoder.encode(getPassword()));
            user.setActive(true);
            user = userRepository.save(user);

            UserRole ur = new UserRole();
            ur.setUser(user);
            ur.setRole(superAdminRole);
            ur.setCompanyId(company.getId());
            ur.setActive(true);
            
            user.setRoles(new java.util.HashSet<>(java.util.List.of(ur)));
            userRepository.save(user);
            
            log.info("Superadmin user created: superadmin@default");
        }
    }

    private Role createRole(Company company, String code, List<String> permissions) {

        return roleRepository.findByCodeAndCompanyId(code, company.getId())
                .orElseGet(() -> {

                    Role role = new Role();
                    role.setCode(code);
                    role.setCompanyId(company.getId());

                    role = roleRepository.save(role);

                    Set<RolePermission> list = new HashSet<>();

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

        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(getPassword()));
        user.setActive(true);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setCompanyId(company.getId());

        user.setRoles(Set.of(userRole));

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

    // =========================
    // MODULES
    // =========================
    private void seedModules(Company company) {
        if (moduleRepository.count() > 15) return; // Allow seeding for both companies

        Long cid = company.getId();
        var inventory = createModule("INVENTORY", "Inventory", "Manage stocks and business units.", "inventory", "Package", 1, cid);
        createMenu(inventory, "Stock Items", "inventory", "Warehouse", 1, cid);
        createMenu(inventory, "Business Units", "inventory/units", "Package", 2, cid);
        createMenu(inventory, "Stock Reports", "inventory/reports", "BarChart2", 3, cid);

        var sales = createModule("SALES_INVOICING", "Sales & Invoicing", "Track orders and VAT.", "sales", "ShoppingCart", 2, cid);
        createMenu(sales, "Dashboard", "sales", "ShoppingCart", 1, cid);

        var finance = createModule("FINANCE", "Finance", "Revenue and credit tracking.", "finance", "CreditCard", 3, cid);
        createMenu(finance, "Dashboard", "finance", "CreditCard", 1, cid);

        var iam = createModule("IAM", "I A M", "Identity & Access Management.", "identity-access", "ShieldCheck", 4, cid);
        createMenu(iam, "Dashboard", "identity-access", "ShieldCheck", 1, cid);

        var settings = createModule("SETTINGS", "Settings", "Application configuration and preferences.", "settings", "Settings", 5, cid);
        createMenu(settings, "Dashboard", "settings", "Settings", 1, cid);

        var salesCrm = createModule("SALES_CRM", "Sales CRM", "Customer relationship management for sales.", "sales-crm", "Target", 6, cid);
        createMenu(salesCrm, "Dashboard", "sales-crm", "Target", 1, cid);

        var serviceCrm = createModule("SERVICE_CRM", "Service CRM", "Customer support and service.", "service-crm", "Headset", 7, cid);
        createMenu(serviceCrm, "Dashboard", "service-crm", "Headset", 1, cid);

        var mfg = createModule("MANUFACTURING", "Manufacturing", "Production and manufacturing workflows.", "manufacturing", "Factory", 8, cid);
        createMenu(mfg, "Dashboard", "manufacturing", "Factory", 1, cid);

        var retail = createModule("RETAIL_POS", "Retail POS", "Point of sale for retail.", "retail-pos", "Store", 9, cid);
        createMenu(retail, "Dashboard", "retail-pos", "Store", 1, cid);

        var restPos = createModule("RESTAURANT_POS", "Restaurant POS", "Point of sale for restaurants.", "restaurant-pos", "Utensils", 10, cid);
        createMenu(restPos, "Dashboard", "restaurant-pos", "Utensils", 1, cid);

        var hr = createModule("HR", "HR module", "Human resources and payroll.", "hr", "Users", 11, cid);
        createMenu(hr, "Dashboard", "hr", "Users", 1, cid);

        log.info("Modules and menus seeded successfully.");
    }

    private org.enterprise.security.entity.Module createModule(String code, String name, String description, String route, String icon, int order, Long companyId) {
        org.enterprise.security.entity.Module module = new org.enterprise.security.entity.Module();
        module.setCode(code);
        module.setName(name);
        module.setCategory(description);
        module.setRoute(route);
        module.setIcon(icon);
        module.setDisplayOrder(order);
        module.setActive(true);
        module.setInstalled(true);
        module.setVisibleInLauncher(true);
        module.setCompanyId(companyId);
        module.setCompanyId(companyId);
        return moduleRepository.save(module);
    }

    private void createMenu(org.enterprise.security.entity.Module module, String name, String path, String icon, int order, Long companyId) {
        org.enterprise.security.entity.Menu menu = new org.enterprise.security.entity.Menu();
        menu.setCode(name.toUpperCase().replace(" ", "_"));
        menu.setName(name);
        menu.setPath(path);
        menu.setIcon(icon);
        menu.setDisplayOrder(order);
        menu.setVisible(true);
        menu.setModule(module);
        menu.setCompanyId(companyId);
        menuRepository.save(menu);
    }
}