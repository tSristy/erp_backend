package org.enterprise.common.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.workflow.repository.WorkflowStepRepository;
import jakarta.transaction.Transactional;
import org.enterprise.finance.repository.AccountRepository;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import org.enterprise.security.entity.*;
import org.enterprise.security.repository.*;
import org.enterprise.workflow.entity.WorkflowDefinition;
import org.enterprise.workflow.entity.WorkflowStep;
import org.springframework.context.annotation.Profile;
import org.enterprise.finance.enums.AccountType;
import java.util.HashSet;
import org.enterprise.workflow.repository.WorkflowDefinitionRepository;
import org.enterprise.organization.repository.CompanyRepository;
import java.util.Set;
import org.enterprise.finance.entity.Account;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import org.enterprise.organization.entity.Company;
import org.enterprise.finance.entity.*;
import org.enterprise.finance.enums.*;
import org.enterprise.inventory.entity.*;
import org.enterprise.inventory.enums.ProductType;
import org.enterprise.inventory.enums.*;
import org.enterprise.production.entity.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Order(1)
@RequiredArgsConstructor
public class MasterDataSeeder implements CommandLineRunner {

    private final EntityManager em;
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
    private final LoginAuditRepository loginAuditRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // Only run if the database is empty (no accounts exist)
        Long accountCount = em.createQuery("SELECT COUNT(a) FROM Account a", Long.class).getSingleResult();
        if (accountCount > 0) {
            log.info("Master data already exists. Skipping Seeder.");
            return;
        }

        log.info("Starting Master Data Seeder...");

        // 1. Companies
        Company comp1 = new Company();
        comp1.setCode("C01"); comp1.setName("Acme Corp");
        em.persist(comp1);

        Company comp2 = new Company();
        comp2.setCode("C02"); comp2.setName("Globex Inc");
        em.persist(comp2);

        Long companyId = comp1.getId();

        // 2. Fiscal Year & Periods
        FiscalYear fy = new FiscalYear();
        fy.setCompanyId(companyId); fy.setYearCode("FY26"); fy.setStartDate(LocalDate.of(2026, 1, 1)); fy.setEndDate(LocalDate.of(2026, 12, 31));
        em.persist(fy);

        for (int i = 1; i <= 12; i++) {
            FiscalPeriod fp = new FiscalPeriod();
            fp.setCompanyId(companyId); fp.setFiscalYear(fy); fp.setPeriodName("P" + i); fp.setStartDate(LocalDate.of(2026, i, 1)); fp.setEndDate(LocalDate.of(2026, i, 1).plusMonths(1).minusDays(1));
            em.persist(fp);
        }

        // 3. Profit Center
        ProfitCenter pc1 = new ProfitCenter(); pc1.setCompanyId(companyId); pc1.setCode("PC01"); pc1.setName("Main Profit Center"); em.persist(pc1);

        // 4. Cost Center
        CostCenter cc1 = new CostCenter(); cc1.setCompanyId(companyId); cc1.setCode("CC01"); cc1.setName("IT Dept"); em.persist(cc1);
        CostCenter cc2 = new CostCenter(); cc2.setCompanyId(companyId); cc2.setCode("CC02"); cc2.setName("HR Dept"); em.persist(cc2);

        // 5. Chart of Accounts
        Account currentAssets = createAccount(companyId, "1000", "Current Assets", AccountType.ASSET, null, false);
        Account cash = createAccount(companyId, "1010", "Cash", AccountType.ASSET, currentAssets, true);
        Account savingsCash = createAccount(companyId, "1011", "Savings Cash", AccountType.ASSET, currentAssets, true);

        Account ar = createAccount(companyId, "1020", "Accounts Receivable", AccountType.ASSET, currentAssets, true);
        Account inventoryAcc = createAccount(companyId, "1030", "Inventory", AccountType.ASSET, currentAssets, true);

        Account nonCurrentAssets = createAccount(companyId, "1500", "Non-Current Assets", AccountType.ASSET, null, false);
        Account ppe = createAccount(companyId, "1510", "PPE", AccountType.ASSET, nonCurrentAssets, true);
        Account accDep = createAccount(companyId, "1520", "Accumulated Depreciation", AccountType.ASSET, nonCurrentAssets, true);

        Account currentLiabilities = createAccount(companyId, "2000", "Current Liabilities", AccountType.LIABILITY, null, false);
        Account ap = createAccount(companyId, "2010", "Accounts Payable", AccountType.LIABILITY, currentLiabilities, true);
        
        Account nonCurrentLiabilities = createAccount(companyId, "2500", "Non-Current Liabilities", AccountType.LIABILITY, null, false);
        Account loans = createAccount(companyId, "2510", "Loans", AccountType.LIABILITY, nonCurrentLiabilities, true);

        Account equity = createAccount(companyId, "3000", "Equity", AccountType.EQUITY, null, false);
        Account shareCap = createAccount(companyId, "3010", "Share Capital", AccountType.EQUITY, equity, true);
        Account retainedE = createAccount(companyId, "3020", "Retained Earnings", AccountType.EQUITY, equity, true);

        Account revenue = createAccount(companyId, "4000", "Revenue", AccountType.INCOME, null, false);
        Account salesRev = createAccount(companyId, "4010", "Sales", AccountType.INCOME, revenue, true);

        Account expenses = createAccount(companyId, "5000", "Expenses", AccountType.EXPENSE, null, false);
        Account cogs = createAccount(companyId, "5010", "COGS", AccountType.EXPENSE, expenses, true);
        Account salary = createAccount(companyId, "5020", "Salary", AccountType.EXPENSE, expenses, true);

        // 6. Statement Setup
        createSetup(companyId, ReportType.BALANCE_SHEET, 10, null, 1, "ASSETS", CalculationType.HEADER, null, true, false, List.of());
        createSetup(companyId, ReportType.BALANCE_SHEET, 20, 10, 2, "Current Assets", CalculationType.HEADER, null, true, false, List.of());
        createSetup(companyId, ReportType.BALANCE_SHEET, 30, 20, 3, "Cash", CalculationType.ACCOUNT_SUM, null, false, false, List.of(cash));
        createSetup(companyId, ReportType.BALANCE_SHEET, 40, 20, 3, "Accounts Receivable", CalculationType.ACCOUNT_SUM, null, false, false, List.of(ar));
        createSetup(companyId, ReportType.BALANCE_SHEET, 50, 20, 3, "Inventory", CalculationType.ACCOUNT_SUM, null, false, false, List.of(inventoryAcc));
        createSetup(companyId, ReportType.BALANCE_SHEET, 60, 20, 2, "Total Current Assets", CalculationType.FORMULA, "30 + 40 + 50", true, false, List.of());
        createSetup(companyId, ReportType.BALANCE_SHEET, 70, 10, 2, "Non-Current Assets", CalculationType.HEADER, null, true, false, List.of());
        createSetup(companyId, ReportType.BALANCE_SHEET, 80, 70, 3, "PPE", CalculationType.ACCOUNT_SUM, null, false, false, List.of(ppe, accDep));
        createSetup(companyId, ReportType.BALANCE_SHEET, 90, 70, 2, "Total Non-Current Assets", CalculationType.FORMULA, "80", true, false, List.of());
        createSetup(companyId, ReportType.BALANCE_SHEET, 100, 10, 1, "TOTAL ASSETS", CalculationType.FORMULA, "60 + 90", true, true, List.of());

        createSetup(companyId, ReportType.BALANCE_SHEET, 200, null, 1, "LIABILITIES & EQUITY", CalculationType.HEADER, null, true, false, List.of());
        createSetup(companyId, ReportType.BALANCE_SHEET, 210, 200, 2, "Current Liabilities", CalculationType.ACCOUNT_SUM, null, false, false, List.of(ap));
        createSetup(companyId, ReportType.BALANCE_SHEET, 220, 200, 2, "Non-Current Liabilities", CalculationType.ACCOUNT_SUM, null, false, false, List.of(loans));
        createSetup(companyId, ReportType.BALANCE_SHEET, 230, 200, 2, "Equity", CalculationType.ACCOUNT_SUM, null, false, false, List.of(shareCap, retainedE));
        createSetup(companyId, ReportType.BALANCE_SHEET, 240, 200, 1, "TOTAL LIAB & EQ", CalculationType.FORMULA, "(210 + 220 + 230) * -1", true, true, List.of());


        // INCOME STATEMENT
        createSetup(companyId, ReportType.INCOME_STATEMENT, 300, null, 1, "REVENUE", CalculationType.HEADER, null, true, false, List.of());
        createSetup(companyId, ReportType.INCOME_STATEMENT, 310, 300, 2, "Sales Revenue", CalculationType.ACCOUNT_SUM, null, false, false, List.of(salesRev));
        createSetup(companyId, ReportType.INCOME_STATEMENT, 320, 300, 1, "TOTAL REVENUE", CalculationType.FORMULA, "310", true, false, List.of());

        createSetup(companyId, ReportType.INCOME_STATEMENT, 400, null, 1, "EXPENSES", CalculationType.HEADER, null, true, false, List.of());
        createSetup(companyId, ReportType.INCOME_STATEMENT, 410, 400, 2, "Cost of Goods Sold", CalculationType.ACCOUNT_SUM, null, false, false, List.of(cogs));
        createSetup(companyId, ReportType.INCOME_STATEMENT, 420, 400, 1, "GROSS PROFIT", CalculationType.FORMULA, "320 - 410", true, false, List.of());

        createSetup(companyId, ReportType.INCOME_STATEMENT, 430, 400, 2, "Salary Expense", CalculationType.ACCOUNT_SUM, null, false, false, List.of(salary));
        createSetup(companyId, ReportType.INCOME_STATEMENT, 440, 400, 1, "TOTAL EXPENSES", CalculationType.FORMULA, "410 + 430", true, false, List.of());

        createSetup(companyId, ReportType.INCOME_STATEMENT, 500, null, 1, "NET INCOME", CalculationType.FORMULA, "320 - 440", true, true, List.of());

        // CASH FLOW
        createSetup(companyId, ReportType.CASH_FLOW, 600, null, 1, "CASH FLOWS FROM OPERATING ACTIVITIES", CalculationType.HEADER, null, true, false, List.of());
        createSetup(companyId, ReportType.CASH_FLOW, 610, 600, 2, "Net Income", CalculationType.ACCOUNT_SUM, null, false, false, List.of(salesRev, cogs, salary));
        createSetup(companyId, ReportType.CASH_FLOW, 620, 600, 2, "Changes in Working Capital", CalculationType.ACCOUNT_SUM, null, false, false, List.of(ar, inventoryAcc, ap));
        createSetup(companyId, ReportType.CASH_FLOW, 630, 600, 1, "Net Cash from Operating Activities", CalculationType.FORMULA, "610 + 620", true, false, List.of());

        createSetup(companyId, ReportType.CASH_FLOW, 700, null, 1, "CASH FLOWS FROM INVESTING ACTIVITIES", CalculationType.HEADER, null, true, false, List.of());
        createSetup(companyId, ReportType.CASH_FLOW, 710, 700, 2, "Purchase of PPE", CalculationType.ACCOUNT_SUM, null, false, false, List.of(ppe));
        createSetup(companyId, ReportType.CASH_FLOW, 720, 700, 1, "Net Cash from Investing Activities", CalculationType.FORMULA, "710", true, false, List.of());

        createSetup(companyId, ReportType.CASH_FLOW, 800, null, 1, "CASH FLOWS FROM FINANCING ACTIVITIES", CalculationType.HEADER, null, true, false, List.of());
        createSetup(companyId, ReportType.CASH_FLOW, 810, 800, 2, "Loans & Borrowings", CalculationType.ACCOUNT_SUM, null, false, false, List.of(loans));
        createSetup(companyId, ReportType.CASH_FLOW, 820, 800, 2, "Share Capital Issue", CalculationType.ACCOUNT_SUM, null, false, false, List.of(shareCap));
        createSetup(companyId, ReportType.CASH_FLOW, 830, 800, 1, "Net Cash from Financing Activities", CalculationType.FORMULA, "810 + 820", true, false, List.of());

        createSetup(companyId, ReportType.CASH_FLOW, 900, null, 1, "NET INCREASE (DECREASE) IN CASH", CalculationType.FORMULA, "630 + 720 + 830", true, true, List.of());


        // 7. Bank Accounts
        BankAccount bank1 = new BankAccount(); bank1.setCompanyId(companyId); bank1.setAccountName("Main Checking"); bank1.setAccountNumber("123456"); bank1.setAccount(cash); em.persist(bank1);
        BankAccount bank2 = new BankAccount(); bank2.setCompanyId(companyId); bank2.setAccountName("Savings"); bank2.setAccountNumber("987654"); bank2.setAccount(savingsCash); em.persist(bank2);

        // 8. Categories, Brands, Attributes, UOM
        Category cat1 = new Category(); cat1.setCompanyId(companyId); cat1.setCode("ELEC"); cat1.setName("Electronics"); em.persist(cat1);
        Category cat2 = new Category(); cat2.setCompanyId(companyId); cat2.setCode("FURN"); cat2.setName("Furniture"); em.persist(cat2);

        Brand b1 = new Brand(); b1.setCompanyId(companyId); b1.setCode("B01"); b1.setName("Samsung"); em.persist(b1);
        Brand b2 = new Brand(); b2.setCompanyId(companyId); b2.setCode("B02"); b2.setName("Apple"); em.persist(b2);

        Attribute attr1 = new Attribute(); attr1.setCompanyId(companyId); attr1.setCode("COLOR"); attr1.setName("Color"); em.persist(attr1);
        Attribute attr2 = new Attribute(); attr2.setCompanyId(companyId); attr2.setCode("SIZE"); attr2.setName("Size"); em.persist(attr2);

        UnitOfMeasure uom1 = new UnitOfMeasure(); uom1.setCompanyId(companyId); uom1.setCode("PCS"); uom1.setName("Pieces"); em.persist(uom1);
        UnitOfMeasure uom2 = new UnitOfMeasure(); uom2.setCompanyId(companyId); uom2.setCode("KG"); uom2.setName("Kilograms"); em.persist(uom2);

        // 9. Products & Variants (10 Products)
        for (int i = 1; i <= 10; i++) {
            Product p = new Product();
            p.setCompanyId(companyId);
            p.setSku("P00" + i);
            p.setName("Product " + i);
            p.setCategory(i <= 5 ? cat1 : cat2);
            p.setBrand(i % 2 == 0 ? b1 : b2);
            p.setBaseUom(uom1);
            p.setCostingMethod(CostingMethod.FIFO);
            p.setProductType(i <= 3 ? ProductType.FINISHED_GOOD : ProductType.RAW_MATERIAL);
            em.persist(p);

            if (i <= 3) {
                ProductVariant pv1 = new ProductVariant(); pv1.setCompanyId(companyId); pv1.setProduct(p); pv1.setSku("P00" + i + "-RED"); em.persist(pv1);
                ProductVariant pv2 = new ProductVariant(); pv2.setCompanyId(companyId); pv2.setProduct(p); pv2.setSku("P00" + i + "-BLUE"); em.persist(pv2);
            }
        }

        // 10. Business Partners (5 Cust, 5 Vend)
        for (int i = 1; i <= 5; i++) {
            BusinessPartner cust = new BusinessPartner(); cust.setCompanyId(companyId); cust.setCode("CUST0" + i); cust.setName("Customer " + i); em.persist(cust);
            BusinessPartnerRole roleCust = new BusinessPartnerRole(); roleCust.setCompanyId(companyId); roleCust.setPartner(cust); roleCust.setRole(BusinessPartnerRole.RoleType.CUSTOMER); em.persist(roleCust);

            BusinessPartner vend = new BusinessPartner(); vend.setCompanyId(companyId); vend.setCode("VEND0" + i); vend.setName("Vendor " + i); em.persist(vend);
            BusinessPartnerRole roleVend = new BusinessPartnerRole(); roleVend.setCompanyId(companyId); roleVend.setPartner(vend); roleVend.setRole(BusinessPartnerRole.RoleType.VENDOR); em.persist(roleVend);
        }

        // 11. Warehouse & Locations
        Warehouse w1 = new Warehouse(); w1.setCompanyId(companyId); w1.setCode("WH01"); w1.setName("Main Warehouse"); w1.setInventoryAccount(inventoryAcc); w1.setCogsAccount(cogs); em.persist(w1);
        Location loc1 = new Location(); loc1.setCompanyId(companyId); loc1.setWarehouse(w1); loc1.setCode("L01"); loc1.setName("Aisle 1"); loc1.setType(Location.LocationType.AISLE); em.persist(loc1);

        Warehouse w2 = new Warehouse(); w2.setCompanyId(companyId); w2.setCode("WH02"); w2.setName("Secondary Warehouse"); w2.setInventoryAccount(inventoryAcc); w2.setCogsAccount(cogs); em.persist(w2);

        // 12. WorkCenter & Routing
        WorkCenter wc1 = new WorkCenter(); wc1.setCompanyId(companyId); wc1.setCode("WC01"); wc1.setName("Assembly Line 1"); wc1.setCostPerHour(BigDecimal.valueOf(50)); em.persist(wc1);
        WorkCenter wc2 = new WorkCenter(); wc2.setCompanyId(companyId); wc2.setCode("WC02"); wc2.setName("Packaging Line"); wc2.setCostPerHour(BigDecimal.valueOf(30)); em.persist(wc2);

        Routing r1 = new Routing(); r1.setCompanyId(companyId); r1.setCode("RT01"); r1.setName("Standard Assembly"); em.persist(r1);
        
        RoutingOperation ro1 = new RoutingOperation(); ro1.setCompanyId(companyId); ro1.setRouting(r1); ro1.setSequence(10); ro1.setWorkCenter(wc1); ro1.setOperationName("Assemble"); ro1.setDurationMinutes(60); em.persist(ro1);
        RoutingOperation ro2 = new RoutingOperation(); ro2.setCompanyId(companyId); ro2.setRouting(r1); ro2.setSequence(20); ro2.setWorkCenter(wc2); ro2.setOperationName("Package"); ro2.setDurationMinutes(30); em.persist(ro2);

        
        // --- Appended from DataSeeder ---
        seedPermissions();
        org.enterprise.security.entity.Role adminRole = seedAdminRole(comp1);
        seedAdminUser(comp1, adminRole);
        seedSuperAdmin(comp1);
        seedAccountUsers(comp1);
        seedWorkflow(comp1);
        seedModules(comp1);
        seedModules(comp2);

        log.info("Master Data Seeder completed successfully.");
    }

    private Account createAccount(Long companyId, String code, String name, AccountType type, Account parent, boolean allowPosting) {
        Account acc = new Account();
        acc.setCompanyId(companyId);
        acc.setCode(code);
        acc.setName(name);
        acc.setAccountType(type);
        acc.setParent(parent);
        acc.setAllowPosting(allowPosting);
        acc.setActive(true);
        em.persist(acc);
        return acc;
    }

    private void createSetup(Long companyId, ReportType type, Integer serial, Integer parentSerial, Integer level, 
                             String particulars, CalculationType calcType, String formula, 
                             boolean bold, boolean bottomLine, List<Account> accounts) {
        StatementSetup setup = new StatementSetup();
        setup.setCompanyId(companyId);
        setup.setReportType(type);
        setup.setSerialNo(serial);
        setup.setParentSerialNo(parentSerial);
        setup.setLevelNo(level);
        setup.setParticulars(particulars);
        setup.setCalculationType(calcType);
        setup.setFormula(formula);
        setup.setBold(bold);
        setup.setBottomLine(bottomLine);
        setup.setVisible(true);

        if (accounts != null && !accounts.isEmpty()) {
            List<StatementSetupAccount> mappings = new java.util.ArrayList<>();
            for (Account acc : accounts) {
                StatementSetupAccount mapping = new StatementSetupAccount();
                mapping.setStatementSetup(setup);
                mapping.setAccount(acc);
                mappings.add(mapping);
            }
            setup.setAccounts(mappings);
        }

        em.persist(setup);
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
        createMenu(inventory, "Cost Heads", "inventory/costhead", "DollarSign", 16, cid);
        createMenu(inventory, "Taxes", "inventory/tax", "Percent", 17, cid);
        createMenu(inventory, "Batches", "inventory/batch", "Layers", 18, cid);
        // Reports
        createMenu(inventory, "Reports", "inventory/reports", "BarChart2", 19, cid);
        createReportMenu(inventory, "Inventory Stock", "inventory/reports/stock", "Package", 20, cid);
        createReportMenu(inventory, "Movement Register", "inventory/reports/movement", "BarChart2", 21, cid);
        createReportMenu(inventory, "Inventory Aging", "inventory/reports/aging", "Clock", 22, cid);
        createReportMenu(inventory, "Valuation", "inventory/reports/valuation", "DollarSign", 23, cid);
        createReportMenu(inventory, "Serial Numbers", "inventory/reports/serial", "Target", 24, cid);
        createReportMenu(inventory, "Purchase Summary", "inventory/reports/purchase", "Receipt", 25, cid);

        var sales = createModule("SALES_INVOICING", "Sales & Invoicing", "Track orders and VAT.", "sales", "ShoppingCart", 2, cid);
        createMenu(sales, "Dashboard", "sales", "ShoppingCart", 1, cid);
        createMenu(sales, "Sales Quotations", "sales/salesquotation", "FileText", 2, cid);
        createMenu(sales, "Sales Orders", "sales/salesorder", "ShoppingCart", 3, cid);
        createMenu(sales, "Deliveries", "sales/deliverynote", "Truck", 4, cid);
        createMenu(sales, "Sales Invoices", "sales/salesinvoice", "FileText", 5, cid);
        createMenu(sales, "Reports", "sales/reports", "BarChart2", 6, cid);
        createReportMenu(sales, "Daily Sales", "sales/reports/daily", "Calendar", 7, cid);
        createReportMenu(sales, "By Customer", "sales/reports/customer", "Users", 8, cid);
        createReportMenu(sales, "By Product", "sales/reports/product", "Package", 9, cid);
        createReportMenu(sales, "By Warehouse", "sales/reports/warehouse", "Home", 10, cid);
        createReportMenu(sales, "By Salesperson", "sales/reports/salesperson", "UserCheck", 11, cid);
        createReportMenu(sales, "Profitability", "sales/reports/profitability", "DollarSign", 12, cid);

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
        createMenu(finance, "Reports", "finance/reports", "BarChart2", 14, cid);
        createReportMenu(finance, "GL Ledger", "finance/reports/gl-ledger", "BookOpen", 15, cid);
        createReportMenu(finance, "Customer Ledger", "finance/reports/customer-ledger", "UserCircle", 16, cid);
        createReportMenu(finance, "Vendor Ledger", "finance/reports/vendor-ledger", "Truck", 17, cid);
        createReportMenu(finance, "Universal Subledger", "finance/reports/subledger", "Layers", 18, cid);
        createReportMenu(finance, "Trial Balance", "finance/reports/trial-balance", "Scale", 19, cid);
        createReportMenu(finance, "Balance Sheet", "finance/reports/balance-sheet", "FileText", 20, cid);
        createReportMenu(finance, "Income Statement", "finance/reports/income-statement", "TrendingUp", 21, cid);
        createReportMenu(finance, "Cash Flow", "finance/reports/cash-flow", "RefreshCcw", 22, cid);
        createReportMenu(finance, "Customer Aging", "finance/reports/customer-aging", "Users", 23, cid);
        createReportMenu(finance, "Vendor Aging", "finance/reports/vendor-aging", "Building2", 24, cid);

        var iam = createModule("IAM", "I A M", "Identity & Access Management.", "identity-access", "ShieldCheck", 4, cid);
        createMenu(iam, "Dashboard", "identity-access", "ShieldCheck", 1, cid);

        // Security / IAM
        createMenu(iam, "Users", "identity-access/user", "Users", 7, cid);
        createMenu(iam, "Roles", "identity-access/role", "Shield", 8, cid);
        createMenu(iam, "Permissions", "identity-access/permission", "Key", 9, cid);
        createMenu(iam, "Modules", "identity-access/module", "Grid", 10, cid);
        createMenu(iam, "Menus", "identity-access/menu", "Menu", 11, cid);
        createMenu(iam, "Login Audits", "identity-access/loginaudit", "Activity", 12, cid);
        createMenu(iam, "System Audits", "identity-access/systemaudit", "Database", 13, cid);

        var settings = createModule("SETTINGS", "Settings", "Application configuration and preferences.", "settings", "Settings", 5, cid);
        createMenu(settings, "Dashboard", "settings", "Settings", 1, cid);
        createMenu(settings, "Statement Setup", "settings/statementsetup", "FileText", 2, cid);

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
        createMenu(mfg, "Work Centers", "manufacturing/work-centers", "Monitor", 2, cid);
        createMenu(mfg, "Routings", "manufacturing/routings", "GitCommit", 3, cid);
        createMenu(mfg, "Bill of Materials", "manufacturing/billofmaterials", "ClipboardList", 4, cid);
        createMenu(mfg, "Manufacturing Orders", "manufacturing/orders", "FileStack", 5, cid);
        createMenu(mfg, "Production", "manufacturing/production", "Hammer", 6, cid);
        createMenu(mfg, "Reports", "manufacturing/reports", "BarChart2", 7, cid);
        createReportMenu(mfg, "Daily Production", "manufacturing/reports/daily", "Calendar", 8, cid);
        createReportMenu(mfg, "By Product", "manufacturing/reports/product", "Package", 9, cid);
        createReportMenu(mfg, "By Status", "manufacturing/reports/status", "PieChart", 10, cid);
        createReportMenu(mfg, "Production Yield", "manufacturing/reports/yield", "TrendingUp", 11, cid);
        createReportMenu(mfg, "BOM Usage", "manufacturing/reports/bom", "Layers", 12, cid);

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
        createReportMenu(hr, "Employee Summary", "hr/reports/employee-summary", "Users", 26, cid);
        createReportMenu(hr, "Attendance Report", "hr/reports/attendance", "Clock", 27, cid);
        createReportMenu(hr, "Leave Balance", "hr/reports/leave-balance", "CalendarHeart", 28, cid);
        createReportMenu(hr, "Payroll Summary", "hr/reports/payroll-summary", "DollarSign", 29, cid);

        var workflow = createModule("WORKFLOW", "Workflow", "Approval engines and processes.", "workflow", "Workflow", 12, cid);
        createMenu(workflow, "Dashboard", "workflow", "Workflow", 1, cid);
        createMenu(workflow, "My Tasks", "workflow/my-tasks", "Clock", 2, cid);
        createMenu(workflow, "Definitions", "workflow/definitions", "Settings", 3, cid);

        var organizations = createModule("ORGANIZATIONS", "Organizations", "Manage company profiles, branches, and locations.", "organizations", "Building2", 13, cid);
        createMenu(organizations, "Dashboard", "organizations", "Building2", 1, cid);
        createMenu(organizations, "Company", "organizations/company", "Building", 2, cid);
        createMenu(organizations, "Branches", "organizations/branches", "GitBranch", 3, cid);
        createMenu(organizations, "Locations", "organizations/location", "Map", 4, cid);
        createMenu(organizations, "Warehouse", "organizations/warehouse", "Box", 5, cid);

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
