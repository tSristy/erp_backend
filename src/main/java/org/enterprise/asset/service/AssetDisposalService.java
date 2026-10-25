package org.enterprise.asset.service;

import org.enterprise.asset.entity.AssetCategory;
import org.enterprise.asset.entity.AssetDisposal;
import org.enterprise.asset.entity.FixedAsset;
import org.enterprise.asset.repository.AssetCategoryRepository;
import org.enterprise.asset.repository.AssetDisposalRepository;
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
public class AssetDisposalService extends BaseService<AssetDisposal, Long> {

    private static final Logger log = LoggerFactory.getLogger(AssetDisposalService.class);

    private final AssetDisposalRepository repository;
    private final FixedAssetRepository fixedAssetRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;

    public AssetDisposalService(
            AssetDisposalRepository repository,
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

    public List<AssetDisposal> findAllByCurrentCompany() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId != null) {
            return repository.findByCompanyIdOrderByIdDesc(companyId);
        }
        return repository.findAll();
    }

    @Override
    public AssetDisposal save(AssetDisposal entity) {
        if (entity.getCompanyId() == null) {
            entity.setCompanyId(TenantContext.getCompanyId());
        }
        if (entity.getDisposalNumber() == null || entity.getDisposalNumber().isBlank()) {
            entity.setDisposalNumber("DSP-" + System.currentTimeMillis() % 1000000);
        }
        if (entity.getDisposalDate() == null) {
            entity.setDisposalDate(LocalDate.now());
        }

        boolean isNew = entity.getId() == null;
        FixedAsset targetAsset = null;
        BigDecimal originalCost = BigDecimal.ZERO;
        BigDecimal accumulatedDep = BigDecimal.ZERO;

        if (entity.getAssetId() != null) {
            Optional<FixedAsset> assetOpt = fixedAssetRepository.findById(entity.getAssetId());
            if (assetOpt.isPresent()) {
                FixedAsset asset = assetOpt.get();
                targetAsset = asset;
                entity.setAssetCode(asset.getAssetCode());
                entity.setAssetName(asset.getName());

                originalCost = asset.getPurchaseCost() != null ? asset.getPurchaseCost() : BigDecimal.ZERO;
                accumulatedDep = asset.getAccumulatedDepreciation() != null ? asset.getAccumulatedDepreciation() : BigDecimal.ZERO;
                BigDecimal bookVal = asset.getCurrentBookValue() != null ? asset.getCurrentBookValue() : originalCost.subtract(accumulatedDep);
                BigDecimal saleAmt = entity.getSaleAmount() != null ? entity.getSaleAmount() : BigDecimal.ZERO;
                entity.setBookValueAtDisposal(bookVal);
                entity.setGainLossAmount(saleAmt.subtract(bookVal));

                if (entity.getStatus() == AssetDisposal.DisposalStatus.APPROVED) {
                    if (entity.getDisposalType() == AssetDisposal.DisposalType.WRITE_OFF
                            || entity.getDisposalType() == AssetDisposal.DisposalType.LOST) {
                        asset.setStatus(FixedAsset.AssetStatus.WRITTEN_OFF);
                    } else {
                        asset.setStatus(FixedAsset.AssetStatus.DISPOSED);
                    }
                    asset.setCurrentBookValue(BigDecimal.ZERO);
                    fixedAssetRepository.save(asset);
                }
            }
        }

        AssetDisposal saved = super.save(entity);

        if (isNew && saved.getStatus() == AssetDisposal.DisposalStatus.APPROVED && targetAsset != null) {
            postDisposalJournalEntry(saved, targetAsset, originalCost, accumulatedDep);
        }

        return saved;
    }

    private void postDisposalJournalEntry(
            AssetDisposal disposal,
            FixedAsset asset,
            BigDecimal originalCost,
            BigDecimal accumulatedDep) {
        try {
            if (originalCost.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }
            Long companyId = disposal.getCompanyId() != null ? disposal.getCompanyId() : TenantContext.getCompanyId();
            AssetCategory category = null;
            if (asset.getCategoryId() != null) {
                category = assetCategoryRepository.findById(asset.getCategoryId()).orElse(null);
            }

            Account assetAccount = null;
            if (category != null && category.getAssetAccountId() != null) {
                assetAccount = accountRepository.findById(category.getAssetAccountId()).orElse(null);
            }
            if (assetAccount == null && companyId != null) {
                assetAccount = accountRepository.findByCodeAndCompanyId("1510", companyId);
            }

            Account accumulatedDepAccount = null;
            if (category != null && category.getAccumulatedDepAccountId() != null) {
                accumulatedDepAccount = accountRepository.findById(category.getAccumulatedDepAccountId()).orElse(null);
            }
            if (accumulatedDepAccount == null && companyId != null) {
                accumulatedDepAccount = accountRepository.findByCodeAndCompanyId("1520", companyId);
            }

            Account cashAccount = companyId != null
                    ? accountRepository.findByCodeAndCompanyId("1010", companyId)
                    : null;
            Account expenseAccount = null;
            if (companyId != null) {
                expenseAccount = accountRepository.findByCodeAndCompanyId("5030", companyId);
                if (expenseAccount == null) {
                    expenseAccount = accountRepository.findByCodeAndCompanyId("5010", companyId);
                }
            }
            Account revenueAccount = companyId != null
                    ? accountRepository.findByCodeAndCompanyId("4010", companyId)
                    : null;

            if (assetAccount == null) {
                log.warn("Skipping GL JournalEntry for AssetDisposal {}: missing Fixed Asset GL account", disposal.getDisposalNumber());
                return;
            }

            JournalEntry journal = new JournalEntry();
            journal.setCompanyId(companyId);
            journal.setPostingDate(disposal.getDisposalDate() != null ? disposal.getDisposalDate() : LocalDate.now());
            journal.setReferenceType("ASSET_DISPOSAL");
            journal.setReferenceId(disposal.getId());
            journal.setStatus(JournalStatus.POSTED);

            List<JournalEntryLine> lines = new ArrayList<>();

            // 1. Credit Fixed Asset Cost account for original cost
            JournalEntryLine creditAssetLine = new JournalEntryLine();
            creditAssetLine.setJournalEntry(journal);
            creditAssetLine.setCompanyId(companyId);
            creditAssetLine.setAccount(assetAccount);
            creditAssetLine.setDebit(BigDecimal.ZERO);
            creditAssetLine.setCredit(originalCost);
            lines.add(creditAssetLine);

            // 2. Debit Accumulated Depreciation for accumulated depreciation
            if (accumulatedDep.compareTo(BigDecimal.ZERO) > 0 && accumulatedDepAccount != null) {
                JournalEntryLine debitAccDepLine = new JournalEntryLine();
                debitAccDepLine.setJournalEntry(journal);
                debitAccDepLine.setCompanyId(companyId);
                debitAccDepLine.setAccount(accumulatedDepAccount);
                debitAccDepLine.setDebit(accumulatedDep);
                debitAccDepLine.setCredit(BigDecimal.ZERO);
                lines.add(debitAccDepLine);
            }

            // 3. Debit Cash for Sale Proceeds (if any)
            BigDecimal saleAmount = disposal.getSaleAmount() != null ? disposal.getSaleAmount() : BigDecimal.ZERO;
            if (saleAmount.compareTo(BigDecimal.ZERO) > 0 && cashAccount != null) {
                JournalEntryLine debitCashLine = new JournalEntryLine();
                debitCashLine.setJournalEntry(journal);
                debitCashLine.setCompanyId(companyId);
                debitCashLine.setAccount(cashAccount);
                debitCashLine.setDebit(saleAmount);
                debitCashLine.setCredit(BigDecimal.ZERO);
                lines.add(debitCashLine);
            }

            // 4. Balance Gain or Loss on Disposal
            BigDecimal netDiff = originalCost.subtract(accumulatedDep).subtract(saleAmount);
            if (netDiff.compareTo(BigDecimal.ZERO) > 0 && expenseAccount != null) {
                // Loss on disposal -> Debit Expense
                JournalEntryLine lossLine = new JournalEntryLine();
                lossLine.setJournalEntry(journal);
                lossLine.setCompanyId(companyId);
                lossLine.setAccount(expenseAccount);
                lossLine.setDebit(netDiff);
                lossLine.setCredit(BigDecimal.ZERO);
                lines.add(lossLine);
            } else if (netDiff.compareTo(BigDecimal.ZERO) < 0 && revenueAccount != null) {
                // Gain on disposal -> Credit Revenue
                JournalEntryLine gainLine = new JournalEntryLine();
                gainLine.setJournalEntry(journal);
                gainLine.setCompanyId(companyId);
                gainLine.setAccount(revenueAccount);
                gainLine.setDebit(BigDecimal.ZERO);
                gainLine.setCredit(netDiff.abs());
                lines.add(gainLine);
            }

            journal.setLines(lines);
            journalEntryService.save(journal);
        } catch (Exception ex) {
            log.warn("Could not auto-post Finance GL JournalEntry for AssetDisposal {}: {}", disposal.getDisposalNumber(), ex.getMessage());
        }
    }
}
