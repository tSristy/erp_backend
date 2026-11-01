package org.enterprise.asset.service;

import org.enterprise.asset.entity.AssetCategory;
import org.enterprise.asset.entity.AssetDepreciationEntry;
import org.enterprise.asset.entity.FixedAsset;
import org.enterprise.asset.repository.AssetCategoryRepository;
import org.enterprise.asset.repository.AssetDepreciationEntryRepository;
import org.enterprise.asset.repository.FixedAssetRepository;
import org.enterprise.common.util.TenantContext;
import org.enterprise.finance.entity.Account;
import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.repository.AccountRepository;
import org.enterprise.finance.service.JournalEntryService;
import org.enterprise.inventory.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AssetDepreciationEntryService extends BaseService<AssetDepreciationEntry, Long> {

    private static final Logger log = LoggerFactory.getLogger(AssetDepreciationEntryService.class);

    private final AssetDepreciationEntryRepository repository;
    private final FixedAssetRepository fixedAssetRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;

    public AssetDepreciationEntryService(
            AssetDepreciationEntryRepository repository,
            FixedAssetRepository fixedAssetRepository,
            AssetCategoryRepository assetCategoryRepository,
            AccountRepository accountRepository,
            JournalEntryService journalEntryService) {
        super(repository);
        this.repository = repository;
        this.fixedAssetRepository = fixedAssetRepository;
        this.assetCategoryRepository = assetCategoryRepository;
        this.accountRepository = accountRepository;
        this.journalEntryService = journalEntryService;
    }

    public List<AssetDepreciationEntry> findAllByCurrentCompany() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId != null) {
            return repository.findByCompanyIdOrderByIdDesc(companyId);
        }
        return repository.findAll();
    }

    @Override
    public AssetDepreciationEntry save(AssetDepreciationEntry entity) {
        if (entity.getCompanyId() == null) {
            entity.setCompanyId(TenantContext.getCompanyId());
        }
        if (entity.getEntryNumber() == null || entity.getEntryNumber().isBlank()) {
            entity.setEntryNumber("DEP-" + System.currentTimeMillis() % 1000000);
        }
        if (entity.getPeriodDate() == null) {
            entity.setPeriodDate(LocalDate.now());
        }

        boolean isNew = entity.getId() == null;
        FixedAsset targetAsset = null;

        if (entity.getAssetId() != null) {
            Optional<FixedAsset> assetOpt = fixedAssetRepository.findById(entity.getAssetId());
            if (assetOpt.isPresent()) {
                FixedAsset asset = assetOpt.get();
                targetAsset = asset;
                entity.setAssetCode(asset.getAssetCode());
                entity.setAssetName(asset.getName());

                BigDecimal depAmt = entity.getDepreciationAmount() != null ? entity.getDepreciationAmount() : BigDecimal.ZERO;
                BigDecimal currentAccDep = asset.getAccumulatedDepreciation() != null ? asset.getAccumulatedDepreciation() : BigDecimal.ZERO;
                BigDecimal purchaseCost = asset.getPurchaseCost() != null ? asset.getPurchaseCost() : BigDecimal.ZERO;
                BigDecimal salvageVal = asset.getSalvageValue() != null ? asset.getSalvageValue() : BigDecimal.ZERO;

                BigDecimal newAccDep = currentAccDep.add(depAmt);
                BigDecimal newBookVal = purchaseCost.subtract(newAccDep).max(salvageVal);

                entity.setAccumulatedDepreciationAfter(newAccDep);
                entity.setBookValueAfter(newBookVal);

                if (isNew && entity.getStatus() == AssetDepreciationEntry.DepreciationStatus.POSTED) {
                    asset.setAccumulatedDepreciation(newAccDep);
                    asset.setCurrentBookValue(newBookVal);
                    fixedAssetRepository.save(asset);
                }
            }
        }

        AssetDepreciationEntry saved = super.save(entity);

        if (isNew
                && saved.getStatus() == AssetDepreciationEntry.DepreciationStatus.POSTED
                && saved.getDepreciationAmount() != null
                && saved.getDepreciationAmount().compareTo(BigDecimal.ZERO) > 0) {
            postDepreciationJournalEntry(saved, targetAsset);
        }

        return saved;
    }

    public List<AssetDepreciationEntry> runBulkDepreciation(
            LocalDate periodDate,
            String frequency,
            String remarks,
            List<Long> assetIds) {
        Long companyId = TenantContext.getCompanyId();
        LocalDate effectiveDate = periodDate != null ? periodDate : LocalDate.now();
        String freq = frequency != null ? frequency.trim().toUpperCase() : "MONTHLY";

        List<FixedAsset> allAssets = companyId != null
                ? fixedAssetRepository.findByCompanyIdOrderByIdDesc(companyId)
                : fixedAssetRepository.findAll();

        List<AssetDepreciationEntry> createdEntries = new ArrayList<>();
        int seq = 1;

        for (FixedAsset asset : allAssets) {
            if (asset.getStatus() != FixedAsset.AssetStatus.ACTIVE) {
                continue;
            }
            if (assetIds != null && !assetIds.isEmpty() && !assetIds.contains(asset.getId())) {
                continue;
            }
            if (asset.getDepreciationMethod() == AssetCategory.DepreciationMethod.NO_DEPRECIATION) {
                continue;
            }
            if (repository.existsByAssetIdAndPeriodDate(asset.getId(), effectiveDate)) {
                continue;
            }

            BigDecimal depAmount = calculatePeriodicDepreciation(asset, freq);
            if (depAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            AssetDepreciationEntry entry = new AssetDepreciationEntry();
            entry.setCompanyId(companyId != null ? companyId : asset.getCompanyId());
            entry.setEntryNumber(String.format("DEP-%s-%03d",
                    effectiveDate.toString().replace("-", ""),
                    (System.currentTimeMillis() % 1000) + (seq++)));
            entry.setAssetId(asset.getId());
            entry.setPeriodDate(effectiveDate);
            entry.setDepreciationAmount(depAmount);
            entry.setStatus(AssetDepreciationEntry.DepreciationStatus.POSTED);
            entry.setRemarks(remarks != null && !remarks.isBlank()
                    ? remarks.trim()
                    : ("Bulk " + freq.toLowerCase() + " depreciation run"));

            createdEntries.add(this.save(entry));
        }

        return createdEntries;
    }

    private BigDecimal calculatePeriodicDepreciation(FixedAsset asset, String frequency) {
        BigDecimal purchaseCost = asset.getPurchaseCost() != null ? asset.getPurchaseCost() : BigDecimal.ZERO;
        BigDecimal salvageVal = asset.getSalvageValue() != null ? asset.getSalvageValue() : BigDecimal.ZERO;
        BigDecimal currentBookVal = asset.getCurrentBookValue() != null ? asset.getCurrentBookValue() : purchaseCost;

        BigDecimal maxRemaining = currentBookVal.subtract(salvageVal);
        if (maxRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        int usefulYears = asset.getUsefulLifeYears() != null && asset.getUsefulLifeYears() > 0
                ? asset.getUsefulLifeYears()
                : 5;

        BigDecimal annualDep;
        if (asset.getDepreciationMethod() == AssetCategory.DepreciationMethod.DECLINING_BALANCE) {
            BigDecimal rate = BigDecimal.valueOf(2.0 / usefulYears);
            annualDep = currentBookVal.multiply(rate);
        } else {
            BigDecimal depreciableBase = purchaseCost.subtract(salvageVal).max(BigDecimal.ZERO);
            annualDep = depreciableBase.divide(BigDecimal.valueOf(usefulYears), 4, java.math.RoundingMode.HALF_UP);
        }

        BigDecimal periodDep;
        if ("DAILY".equalsIgnoreCase(frequency)) {
            periodDep = annualDep.divide(BigDecimal.valueOf(365), 2, java.math.RoundingMode.HALF_UP);
        } else if ("YEARLY".equalsIgnoreCase(frequency)) {
            periodDep = annualDep.setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            periodDep = annualDep.divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);
        }

        return periodDep.min(maxRemaining).max(BigDecimal.ZERO);
    }

    private void postDepreciationJournalEntry(AssetDepreciationEntry entry, FixedAsset asset) {
        try {
            Long companyId = entry.getCompanyId() != null ? entry.getCompanyId() : TenantContext.getCompanyId();
            AssetCategory category = null;
            if (asset != null && asset.getCategoryId() != null) {
                category = assetCategoryRepository.findById(asset.getCategoryId()).orElse(null);
            }

            Account depExpenseAccount = null;
            if (category != null && category.getDepExpenseAccountId() != null) {
                depExpenseAccount = accountRepository.findById(category.getDepExpenseAccountId()).orElse(null);
            }
            if (depExpenseAccount == null && companyId != null) {
                depExpenseAccount = accountRepository.findByCodeAndCompanyId("5030", companyId);
                if (depExpenseAccount == null) {
                    depExpenseAccount = accountRepository.findByCodeAndCompanyId("5010", companyId);
                }
            }

            Account accumulatedDepAccount = null;
            if (category != null && category.getAccumulatedDepAccountId() != null) {
                accumulatedDepAccount = accountRepository.findById(category.getAccumulatedDepAccountId()).orElse(null);
            }
            if (accumulatedDepAccount == null && companyId != null) {
                accumulatedDepAccount = accountRepository.findByCodeAndCompanyId("1520", companyId);
            }

            if (depExpenseAccount == null || accumulatedDepAccount == null) {
                log.warn("Skipping GL JournalEntry for Depreciation {}: missing Expense or Accumulated Depreciation account", entry.getEntryNumber());
                return;
            }

            JournalEntry journal = new JournalEntry();
            journal.setCompanyId(companyId);
            journal.setPostingDate(entry.getPeriodDate() != null ? entry.getPeriodDate() : LocalDate.now());
            journal.setReferenceType("ASSET_DEPRECIATION");
            journal.setReferenceId(entry.getId());
            journal.setStatus(JournalStatus.POSTED);

            List<JournalEntryLine> lines = new ArrayList<>();

            JournalEntryLine debitLine = new JournalEntryLine();
            debitLine.setJournalEntry(journal);
            debitLine.setCompanyId(companyId);
            debitLine.setAccount(depExpenseAccount);
            debitLine.setDebit(entry.getDepreciationAmount());
            debitLine.setCredit(BigDecimal.ZERO);
            lines.add(debitLine);

            JournalEntryLine creditLine = new JournalEntryLine();
            creditLine.setJournalEntry(journal);
            creditLine.setCompanyId(companyId);
            creditLine.setAccount(accumulatedDepAccount);
            creditLine.setDebit(BigDecimal.ZERO);
            creditLine.setCredit(entry.getDepreciationAmount());
            lines.add(creditLine);

            journal.setLines(lines);
            journalEntryService.save(journal);
        } catch (Exception ex) {
            log.warn("Could not auto-post Finance GL JournalEntry for AssetDepreciationEntry {}: {}", entry.getEntryNumber(), ex.getMessage());
        }
    }
}
