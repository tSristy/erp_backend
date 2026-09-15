package org.enterprise.common.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
}
