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

    @Override
    public AssetDepreciationEntry save(AssetDepreciationEntry entity) {
        if (entity.getCompanyId() == null) {
            entity.setCompanyId(TenantContext.getCompanyId());
        }
        if (entity.getEntryNo() == null || entity.getEntryNo().isBlank()) {
            entity.setEntryNo("DEP-" + System.currentTimeMillis() % 1000000);
        }
        if (entity.getDepreciationDate() == null) {
            entity.setDepreciationDate(LocalDate.now());
        }

        boolean isNew = entity.getId() == null;
        FixedAsset targetAsset = null;

        if (isNew && entity.getFixedAssetId() != null) {
            Optional<FixedAsset> assetOpt = fixedAssetRepository.findById(entity.getFixedAssetId());
            if (assetOpt.isPresent()) {
                FixedAsset asset = assetOpt.get();
                targetAsset = asset;
                BigDecimal opening = asset.getCurrentBookValue() != null
                        ? asset.getCurrentBookValue()
                        : (asset.getPurchaseCost() != null ? asset.getPurchaseCost() : BigDecimal.ZERO);
                BigDecimal depAmt = entity.getDepreciationAmount() != null ? entity.getDepreciationAmount() : BigDecimal.ZERO;
                BigDecimal closing = opening.subtract(depAmt).max(asset.getSalvageValue() != null ? asset.getSalvageValue() : BigDecimal.ZERO);

                entity.setOpeningBookValue(opening);
                entity.setClosingBookValue(closing);

                if (entity.getStatus() == AssetDepreciationEntry.DepreciationEntryStatus.POSTED) {
                    BigDecimal currentAccDep = asset.getAccumulatedDepreciation() != null ? asset.getAccumulatedDepreciation() : BigDecimal.ZERO;
                    asset.setAccumulatedDepreciation(currentAccDep.add(depAmt));
                    asset.setCurrentBookValue(closing);
                    if (closing.compareTo(asset.getSalvageValue() != null ? asset.getSalvageValue() : BigDecimal.ZERO) <= 0) {
                        asset.setStatus(FixedAsset.AssetStatus.FULLY_DEPRECIATED);
                    }
                    fixedAssetRepository.save(asset);
                }
            }
        }

        AssetDepreciationEntry saved = super.save(entity);

        if (isNew
                && saved.getStatus() == AssetDepreciationEntry.DepreciationEntryStatus.POSTED
                && saved.getDepreciationAmount() != null
                && saved.getDepreciationAmount().compareTo(BigDecimal.ZERO) > 0) {
            postDepreciationJournalEntry(saved, targetAsset);
        }

        return saved;
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
                depExpenseAccount = accountRepository.findByCodeAndCompanyId("5030", companyId)
                        .or(() -> accountRepository.findByCodeAndCompanyId("5010", companyId))
                        .orElse(null);
            }

            Account accumulatedDepAccount = null;
            if (category != null && category.getAccumulatedDepAccountId() != null) {
                accumulatedDepAccount = accountRepository.findById(category.getAccumulatedDepAccountId()).orElse(null);
            }
            if (accumulatedDepAccount == null && companyId != null) {
                accumulatedDepAccount = accountRepository.findByCodeAndCompanyId("1520", companyId).orElse(null);
            }

            if (depExpenseAccount == null || accumulatedDepAccount == null) {
                log.warn("Skipping GL JournalEntry for Depreciation {}: missing Expense or Accumulated Depreciation account", entry.getEntryNo());
                return;
            }

            JournalEntry journal = new JournalEntry();
            journal.setCompanyId(companyId);
            journal.setPostingDate(entry.getDepreciationDate() != null ? entry.getDepreciationDate() : LocalDate.now());
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
            log.warn("Could not auto-post Finance GL JournalEntry for AssetDepreciationEntry {}: {}", entry.getEntryNo(), ex.getMessage());
        }
    }
}
