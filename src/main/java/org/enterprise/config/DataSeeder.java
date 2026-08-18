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
import org.enterprise.finance.repository.AccountRepository;
import org.enterprise.finance.entity.Account;
import org.enterprise.finance.enums.AccountType;
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
    private final AccountRepository accountRepository;

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

        seedAccounts(company);

        seedWorkflow(company);
        
        seedModules(company);
        seedModules(acme);

        log.info("Data seeding completed successfully.");
    }

    // =========================
    // ACCOUNTS
    // =========================
    private void seedAccounts(Company company) {
        // Accounts Receivable (Needs Business Partner)
        if (accountRepository.findByCodeAndCompanyId("AR-001", company.getId()) == null) {
            Account ar = new Account();
            ar.setCode("AR-001");
            ar.setName("Accounts Receivable");
            ar.setAccountType(AccountType.ASSET);
            ar.setCompanyId(company.getId());
            ar.setBusinessPartnerRequired(true);
            accountRepository.save(ar);
        }

        // Office Supplies Expense (Needs Cost Center and Project)
        if (accountRepository.findByCodeAndCompanyId("EXP-001", company.getId()) == null) {
            Account exp = new Account();
            exp.setCode("EXP-001");
            exp.setName("Office Supplies Expense");
            exp.setAccountType(AccountType.EXPENSE);
            exp.setCompanyId(company.getId());
            exp.setCostCenterRequired(true);
            exp.setProjectRequired(true);
            accountRepository.save(exp);
        }
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

        createPermission("WORKFLOW_START", "Workflow Start");
        createPermission("WORKFLOW_READ", "Workflow Read");
        createPermission("WORKFLOW_WRITE", "Workflow Write");
        createPermission("WORKFLOW_DELETE", "Workflow Delete");
        createPermission("WORKFLOW_APPROVE", "Workflow Approve");
        createPermission("WORKFLOW_REJECT", "Workflow Reject");
        
        createPermission("REST_POS_READ", "Restaurant POS Read");
        createPermission("REST_POS_WRITE", "Restaurant POS Write");

        createPermission("ROLE_READ", "Role Read");
        createPermission("ROLE_WRITE", "Role Write");
        createPermission("USER_READ", "User Read");
        createPermission("USER_WRITE", "User Write");
        createPermission("MENU_READ", "Menu Read");
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

        Role role = roleRepository.findByCodeAndCompanyId("ADMIN", company.getId())
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setCode("ADMIN");
                    r.setCompanyId(company.getId());
                    return roleRepository.save(r);
                });

        List<String> perms = List.of(
                "INVENTORY_READ",
                "INVENTORY_WRITE",
                "FINANCE_READ",
                "FINANCE_WRITE",
                "ACCOUNT_VIEW",
                "ACCOUNT_READ",
                "ACCOUNT_WRITE",
                "WORKFLOW_START",
                "WORKFLOW_READ",
                "WORKFLOW_WRITE",
                "WORKFLOW_DELETE",
                "WORKFLOW_APPROVE",
                "WORKFLOW_REJECT",
                "REST_POS_READ",
                "REST_POS_WRITE",
                "USER_READ",
                "USER_WRITE",
                "ROLE_READ",
                "ROLE_WRITE"
        );

        Set<RolePermission> existingLinks = role.getRolePermissions();
        if (existingLinks == null) {
            existingLinks = new HashSet<>();
            role.setRolePermissions(existingLinks);
        }
        
        boolean modified = false;

        for (String code : perms) {
            Permission permission = permissionRepository.findByCode(code)
                    .orElseThrow();

            if (!rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
                RolePermission rp = new RolePermission();
                rp.setRole(role);
                rp.setPermission(permission);
                rp.setCompanyId(company.getId());
                rp.setAllowed(true);

                existingLinks.add(rolePermissionRepository.save(rp));
                modified = true;
            }
        }

        if (modified) {
            return roleRepository.save(role);
        }

        return role;
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

        Long cid = company.getId();
        var inventory = createModule("INVENTORY", "Inventory", "Manage stocks and business units.", "inventory", "Package", 1, cid);
        createMenu(inventory, "Dashboard", "inventory", "Package", 1, cid);
        // Transactions
        createMenu(inventory, "Purchase Orders", "inventory/purchaseorder", "ShoppingCart", 2, cid);
        createMenu(inventory, "Goods Receipts", "inventory/goodsreceipt", "PackagePlus", 3, cid);
        createMenu(inventory, "Purchase Invoices", "inventory/purchase-invoice", "FileText", 4, cid);
        createMenu(inventory, "Letters of Credit", "inventory/letter-of-credit", "Globe", 5, cid);
        createMenu(inventory, "Landed Costs", "inventory/landed-cost", "DollarSign", 6, cid);
        createMenu(inventory, "Stock Transfer", "inventory/stocktransferpage", "ArrowRightLeft", 7, cid);
        createMenu(inventory, "Stock Reclassification", "inventory/stockreclassification", "RefreshCcw", 8, cid);
        // Settings / Master Data
        createMenu(inventory, "Products", "inventory/product", "Box", 9, cid);
        createMenu(inventory, "Categories", "inventory/category", "List", 10, cid);
        createMenu(inventory, "Brands", "inventory/brand", "Tag", 11, cid);
        createMenu(inventory, "Attributes", "inventory/attribute", "Sliders", 12, cid);
        createMenu(inventory, "Unit of Measure", "inventory/unitofmeasure", "Ruler", 13, cid);
        createMenu(inventory, "Business Partners", "inventory/businesspartner", "Users", 14, cid);
        createMenu(inventory, "Vendor Details", "inventory/vendordetail", "Briefcase", 15, cid);
        createMenu(inventory, "Cost Heads", "inventory/costhead", "DollarSign", 16, cid);
        createMenu(inventory, "Taxes", "inventory/tax", "Percent", 17, cid);
        // Reports
        createMenu(inventory, "Reports", "inventory/reports", "BarChart2", 18, cid);

        var sales = createModule("SALES_INVOICING", "Sales & Invoicing", "Track orders and VAT.", "sales", "ShoppingCart", 2, cid);
        createMenu(sales, "Dashboard", "sales", "ShoppingCart", 1, cid);
        createMenu(sales, "Sales Quotations", "sales/salesquotation", "FileText", 2, cid);
        createMenu(sales, "Sales Orders", "sales/salesorder", "ShoppingCart", 3, cid);
        createMenu(sales, "Deliveries", "sales/deliverynote", "Truck", 4, cid);
        createMenu(sales, "Sales Invoices", "sales/salesinvoice", "FileText", 5, cid);
        createMenu(sales, "Reports", "sales/reports", "BarChart2", 6, cid);

        var finance = createModule("FINANCE", "Finance", "Revenue and credit tracking.", "finance", "CreditCard", 3, cid);
        createMenu(finance, "Dashboard", "finance", "CreditCard", 1, cid);
        createMenu(finance, "Accounts", "finance/account", "Briefcase", 2, cid);
        createMenu(finance, "Bank Accounts", "finance/bank-account", "Library", 3, cid);
        createMenu(finance, "Cost Centers", "finance/cost-center", "Store", 4, cid);
        createMenu(finance, "Profit Centers", "finance/profit-center", "Factory", 5, cid);
        createMenu(finance, "Projects", "finance/project", "Briefcase", 6, cid);
        createMenu(finance, "Loans", "finance/loan", "CreditCard", 7, cid);
        createMenu(finance, "Internal Orders", "finance/internal-order", "Tag", 8, cid);
        createMenu(finance, "Payment Receipts", "finance/payment-receipt", "Download", 9, cid);
        createMenu(finance, "Payment Vouchers", "finance/payment-voucher", "Upload", 10, cid);
        createMenu(finance, "Journal Vouchers", "finance/journal-voucher", "Upload", 11, cid);
        createMenu(finance, "Customer Aging", "finance/aging/customer", "TrendingUp", 12, cid);
        createMenu(finance, "Vendor Aging", "finance/aging/vendor", "TrendingDown", 13, cid);
        createMenu(finance, "Reports", "finance/reports", "BarChart2", 14, cid);
        createReportMenu(finance, "GL Ledger", "finance/reports/gl-ledger", "BookOpen", 14, cid);
        createReportMenu(finance, "Customer Ledger", "finance/reports/customer-ledger", "UserCircle", 15, cid);
        createReportMenu(finance, "Vendor Ledger", "finance/reports/vendor-ledger", "Truck", 16, cid);
        createReportMenu(finance, "Universal Subledger", "finance/reports/subledger", "Layers", 17, cid);
        createReportMenu(finance, "Trial Balance", "finance/reports/trial-balance", "Scale", 18, cid);
        createReportMenu(finance, "Balance Sheet", "finance/reports/balance-sheet", "FileText", 19, cid);
        createReportMenu(finance, "Income Statement", "finance/reports/income-statement", "TrendingUp", 20, cid);
        createReportMenu(finance, "Cash Flow", "finance/reports/cash-flow", "RefreshCcw", 21, cid);

        var iam = createModule("IAM", "I A M", "Identity & Access Management.", "identity-access", "ShieldCheck", 4, cid);
        createMenu(iam, "Dashboard", "identity-access", "ShieldCheck", 1, cid);

        // Security / IAM
        createMenu(iam, "Users", "identity-access/user", "Users", 7, cid);
        createMenu(iam, "Roles", "identity-access/role", "Shield", 8, cid);
        createMenu(iam, "Permissions", "identity-access/permission", "Key", 9, cid);
        createMenu(iam, "Modules", "identity-access/module", "Grid", 10, cid);
        createMenu(iam, "Menus", "identity-access/menu", "Menu", 11, cid);
        createMenu(iam, "Login Audits", "identity-access/loginaudit", "Activity", 12, cid);

        var settings = createModule("SETTINGS", "Settings", "Application configuration and preferences.", "settings", "Settings", 5, cid);
        createMenu(settings, "Dashboard", "settings", "Settings", 1, cid);

        // Organization
        createMenu(settings, "Company", "settings/company", "Building", 2, cid);
        createMenu(settings, "Branch", "settings/branch", "GitBranch", 3, cid);
        createMenu(settings, "Area", "settings/area", "Map", 4, cid);
        createMenu(settings, "Zone", "settings/zone", "MapPin", 5, cid);
        createMenu(settings, "Territory", "settings/territory", "Globe", 6, cid);

        var salesCrm = createModule("SALES_CRM", "Sales CRM", "Customer relationship management for sales.", "sales-crm", "Target", 6, cid);
        createMenu(salesCrm, "Dashboard", "sales-crm", "Target", 1, cid);
        createMenu(salesCrm, "Leads", "sales-crm/leads", "Users", 2, cid);
        createMenu(salesCrm, "Opportunities", "sales-crm/opportunities", "Briefcase", 3, cid);
        createMenu(salesCrm, "Interactions", "sales-crm/interactions", "MessageSquare", 4, cid);
        createMenu(salesCrm, "Loyalty Programs", "sales-crm/loyalty", "Award", 5, cid);

        var serviceCrm = createModule("SERVICE_CRM", "Service CRM", "Customer support and service.", "service-crm", "Headset", 7, cid);
        createMenu(serviceCrm, "Dashboard", "service-crm", "Headset", 1, cid);
        createMenu(serviceCrm, "Service Requests", "service-crm/servicerequests", "Wrench", 2, cid);
        createMenu(serviceCrm, "Service Estimates", "service-crm/serviceestimates", "FileText", 3, cid);
        createMenu(serviceCrm, "Parts Requisitions", "service-crm/servicepartsrequisitions", "Tool", 4, cid);
        createMenu(serviceCrm, "Maintenance Schedules", "service-crm/maintenanceschedules", "Calendar", 5, cid);
        createMenu(serviceCrm, "Registered Products", "service-crm/registeredproducts", "Box", 6, cid);

        var mfg = createModule("MANUFACTURING", "Manufacturing", "Production and manufacturing workflows.", "manufacturing", "Factory", 8, cid);
        createMenu(mfg, "Dashboard", "manufacturing", "Factory", 1, cid);
        createMenu(mfg, "Bill of Materials", "manufacturing/billofmaterials", "ClipboardList", 2, cid);
        createMenu(mfg, "Manufacturing Orders", "manufacturing/orders", "FileStack", 3, cid);
        createMenu(mfg, "Production", "manufacturing/production", "Hammer", 4, cid);

        var retail = createModule("RETAIL_POS", "Retail POS", "Point of sale for retail.", "retail-pos", "Store", 9, cid);
        createMenu(retail, "Dashboard", "retail-pos", "Store", 1, cid);
        createMenu(retail, "POS Sales", "retail-pos/possales", "ShoppingCart", 2, cid);
        createMenu(retail, "POS Return", "retail-pos/posreturn", "Undo2", 3, cid);

        var restPos = createModule("RESTAURANT_POS", "Restaurant POS", "Point of sale for restaurants.", "restaurant-pos", "Utensils", 10, cid);
        createMenu(restPos, "Dashboard", "restaurant-pos", "Utensils", 1, cid);
        createMenu(restPos, "Orders", "restaurant-pos/orders", "ClipboardList", 2, cid);
        createMenu(restPos, "Menu Items", "restaurant-pos/items", "Coffee", 3, cid);
        createMenu(restPos, "Tables", "restaurant-pos/tables", "Grid", 4, cid);
        createMenu(restPos, "Kitchen Display", "restaurant-pos/kds", "Monitor", 5, cid);

        var hr = createModule("HR", "HR module", "Human resources and payroll.", "hr", "Users", 11, cid);
        createMenu(hr, "Dashboard", "hr", "Users", 1, cid);
        createMenu(hr, "Attendance", "hr/attendance", "Clock", 2, cid);
        createMenu(hr, "Biometric Device", "hr/biometricdevice", "Fingerprint", 3, cid);
        createMenu(hr, "Department", "hr/department", "Building", 4, cid);
        createMenu(hr, "Designation", "hr/designation", "Award", 5, cid);
        createMenu(hr, "Employee", "hr/employee", "Users", 6, cid);
        createMenu(hr, "Employee Confirmation", "hr/employeeconfirmation", "CheckCircle", 7, cid);
        createMenu(hr, "Employee Increment", "hr/employeeincrement", "TrendingUp", 8, cid);
        createMenu(hr, "Employee Loan", "hr/employeeloan", "CreditCard", 9, cid);
        createMenu(hr, "Employee Promotion", "hr/employeepromotion", "Star", 10, cid);
        createMenu(hr, "Employee Roster", "hr/employeeroster", "Calendar", 11, cid);
        createMenu(hr, "Employee Transfer", "hr/employeetransfer", "Repeat", 12, cid);
        createMenu(hr, "Holiday", "hr/holiday", "CalendarHeart", 13, cid);
        createMenu(hr, "Leave Application", "hr/leaveapplication", "FileText", 14, cid);
        createMenu(hr, "Leave Balance", "hr/leavebalance", "Scale", 15, cid);
        createMenu(hr, "Leave Type", "hr/leavetype", "Tag", 16, cid);
        createMenu(hr, "Mobile Attendance", "hr/mobileattendance", "Smartphone", 17, cid);
        createMenu(hr, "Payroll Process", "hr/payrollprocess", "Settings", 18, cid);
        createMenu(hr, "Payslip", "hr/payslip", "FileSignature", 19, cid);
        createMenu(hr, "Provident Fund", "hr/providentfund", "PiggyBank", 20, cid);
        createMenu(hr, "Salary Component", "hr/salarycomponent", "PieChart", 21, cid);
        createMenu(hr, "Shift", "hr/shift", "Clock", 22, cid);
        createMenu(hr, "Tax Slab", "hr/taxslab", "Percent", 23, cid);
        createMenu(hr, "Weekend", "hr/weekend", "CalendarDays", 24, cid);
        createMenu(hr, "Reports", "hr/reports", "BarChart2", 25, cid);

        var workflow = createModule("WORKFLOW", "Workflow", "Approval engines and processes.", "workflow", "Workflow", 12, cid);
        createMenu(workflow, "Dashboard", "workflow", "Workflow", 1, cid);
        createMenu(workflow, "My Tasks", "workflow/my-tasks", "Clock", 2, cid);
        createMenu(workflow, "Definitions", "workflow/definitions", "Settings", 3, cid);

        log.info("Modules and menus seeded successfully.");
    }

    private org.enterprise.security.entity.Module createModule(String code, String name, String description, String route, String icon, int order, Long companyId) {
        return moduleRepository.findByCodeAndCompanyId(code, companyId)
            .orElseGet(() -> {
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
                return moduleRepository.save(module);
            });
    }

    private void createMenu(org.enterprise.security.entity.Module module, String name, String path, String icon, int order, Long companyId) {
        createMenu(module, name, path, icon, order, companyId, false);
    }

    private void createReportMenu(org.enterprise.security.entity.Module module, String name, String path, String icon, int order, Long companyId) {
        createMenu(module, name, path, icon, order, companyId, true);
    }

    private void createMenu(org.enterprise.security.entity.Module module, String name, String path, String icon, int order, Long companyId, boolean isReport) {
        String code = module.getCode() + "_" + name.toUpperCase().replace(" ", "_");
        if (menuRepository.findByCodeAndCompanyId(code, companyId).isPresent()) return;
        
        org.enterprise.security.entity.Menu menu = new org.enterprise.security.entity.Menu();
        menu.setCode(code);
        menu.setName(name);
        menu.setPath(path);
        menu.setIcon(icon);
        menu.setDisplayOrder(order);
        menu.setVisible(true);
        menu.setIsReportMenu(isReport);
        menu.setModule(module);
        menu.setCompanyId(companyId);
        menuRepository.save(menu);
    }
}