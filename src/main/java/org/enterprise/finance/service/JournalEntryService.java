package org.enterprise.finance.service;

import jakarta.persistence.EntityManager;
import org.enterprise.finance.dto.AccountDTO;
import org.enterprise.finance.dto.CostCenterDTO;
import org.enterprise.finance.dto.JournalEntryDTO;
import org.enterprise.finance.dto.JournalEntryLineDTO;
import org.enterprise.finance.dto.ProfitCenterDTO;
import org.enterprise.finance.entity.*;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.repository.AccountBalanceRepository;
import org.enterprise.finance.repository.AccountRepository;
import org.enterprise.finance.repository.DimensionBalanceRepository;
import org.enterprise.finance.repository.FiscalPeriodRepository;
import org.enterprise.finance.repository.InternalOrderRepository;
import org.enterprise.finance.repository.JournalEntryRepository;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.LetterOfCredit;
import org.enterprise.organization.entity.Branch;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final DimensionBalanceRepository dimensionBalanceRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final AccountRepository accountRepository;
    private final InternalOrderRepository internalOrderRepository;
    private final EntityManager entityManager;

    public JournalEntryService(JournalEntryRepository journalEntryRepository,
                               AccountBalanceRepository accountBalanceRepository,
                               DimensionBalanceRepository dimensionBalanceRepository,
                               FiscalPeriodRepository fiscalPeriodRepository,
                               AccountRepository accountRepository,
                               InternalOrderRepository internalOrderRepository,
                               EntityManager entityManager) {
        this.journalEntryRepository = journalEntryRepository;
        this.accountBalanceRepository = accountBalanceRepository;
        this.dimensionBalanceRepository = dimensionBalanceRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.accountRepository = accountRepository;
        this.internalOrderRepository = internalOrderRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<JournalEntryDTO> findAll() {
        return journalEntryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JournalEntryDTO findById(Long id) {
        return journalEntryRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Transactional
    public JournalEntryDTO save(JournalEntryDTO dto) {
        JournalEntry entity = convertToEntity(dto);
        JournalEntry saved = this.save(entity);
        return convertToDTO(saved);
    }

    @Transactional
    public JournalEntry save(JournalEntry journalEntry) {
        if (journalEntry.getJournalNo() == null || journalEntry.getJournalNo().isEmpty()) {
            journalEntry.setJournalNo("JV-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        boolean isNewAndPosted = (journalEntry.getId() == null && journalEntry.getStatus() == JournalStatus.POSTED);
        JournalEntry existing = null;
        if (journalEntry.getId() != null) {
            existing = journalEntryRepository.findById(journalEntry.getId()).orElse(null);
            if (existing != null && (existing.getStatus() == JournalStatus.POSTED || existing.getStatus() == JournalStatus.REVERSED)) {
                throw new RuntimeException("Cannot modify a POSTED or REVERSED journal entry");
            }
        }
        boolean transitioningToPosted = (existing != null && existing.getStatus() != JournalStatus.POSTED && journalEntry.getStatus() == JournalStatus.POSTED);

        validateJournalEntry(journalEntry);
        
        Long companyId = journalEntry.getCompanyId() != null
                ? journalEntry.getCompanyId()
                : org.enterprise.common.util.TenantContext.getCompanyId();
        if (journalEntry.getCompanyId() == null) {
            journalEntry.setCompanyId(companyId);
        }
        if (journalEntry.getLines() != null) {
            for (JournalEntryLine line : journalEntry.getLines()) {
                if (line.getCompanyId() == null) {
                    line.setCompanyId(companyId);
                }
            }
        }
        
        JournalEntry saved = journalEntryRepository.save(journalEntry);

        if (isNewAndPosted || transitioningToPosted) {
            updateLedgerBalances(saved);
        }

        return saved;
    }

    private void updateLedgerBalances(JournalEntry journal) {
        LocalDate postingDate = journal.getPostingDate() != null ? journal.getPostingDate() : LocalDate.now();
        FiscalPeriod period = fiscalPeriodRepository.findActivePeriodByDate(postingDate)
                .or(() -> fiscalPeriodRepository.findPeriodByDate(postingDate))
                .orElse(null);
        Long companyId = journal.getCompanyId() != null
                ? journal.getCompanyId()
                : org.enterprise.common.util.TenantContext.getCompanyId();

        for (JournalEntryLine line : journal.getLines()) {
            BigDecimal lineDebit = line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO;
            BigDecimal lineCredit = line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO;

            // Update Internal Order utilized budget regardless of fiscal period setup
            if (line.getInternalOrder() != null && line.getInternalOrder().getId() != null) {
                internalOrderRepository.findById(line.getInternalOrder().getId()).ifPresent(io -> {
                    BigDecimal currentUtilized = io.getUtilizedAmount() != null ? io.getUtilizedAmount() : BigDecimal.ZERO;
                    BigDecimal netAmount = lineDebit.subtract(lineCredit);
                    io.setUtilizedAmount(currentUtilized.add(netAmount));
                    internalOrderRepository.save(io);
                });
            }

            if (period == null) {
                continue;
            }

            // Update GL Balance
            if (line.getAccount() != null) {
                AccountBalance ab = accountBalanceRepository.findByAccountAndPeriodAndBranch(
                        line.getAccount().getId(), period.getId(), line.getBranch() != null ? line.getBranch().getId() : null
                ).orElseGet(() -> {
                    AccountBalance newAb = new AccountBalance();
                    newAb.setAccount(line.getAccount());
                    newAb.setFiscalPeriod(period);
                    newAb.setFiscalYear(period.getFiscalYear());
                    newAb.setBranch(line.getBranch());
                    newAb.setOpeningDebit(BigDecimal.ZERO);
                    newAb.setOpeningCredit(BigDecimal.ZERO);
                    newAb.setPeriodDebit(BigDecimal.ZERO);
                    newAb.setPeriodCredit(BigDecimal.ZERO);
                    newAb.setClosingDebit(BigDecimal.ZERO);
                    newAb.setClosingCredit(BigDecimal.ZERO);
                    newAb.setCompanyId(companyId);
                    return newAb;
                });

                ab.setPeriodDebit(ab.getPeriodDebit().add(lineDebit));
                ab.setPeriodCredit(ab.getPeriodCredit().add(lineCredit));

                ab.setClosingDebit(ab.getOpeningDebit().add(ab.getPeriodDebit()));
                ab.setClosingCredit(ab.getOpeningCredit().add(ab.getPeriodCredit()));

                accountBalanceRepository.save(ab);
            }

            // Update Sub-Ledger Balance for Business Partner
            if (line.getBusinessPartner() != null && line.getAccount() != null) {
                updateDimensionBalance(period, companyId, line, "BUSINESS_PARTNER", line.getBusinessPartner().getCode(), lineDebit, lineCredit);
            }

            // Update Sub-Ledger / Dimension Balance for Internal Order
            if (line.getInternalOrder() != null && line.getAccount() != null) {
                InternalOrder io = line.getInternalOrder().getCode() != null
                        ? line.getInternalOrder()
                        : internalOrderRepository.findById(line.getInternalOrder().getId()).orElse(null);
                if (io != null && io.getCode() != null) {
                    updateDimensionBalance(period, companyId, line, "INTERNAL_ORDER", io.getCode(), lineDebit, lineCredit);
                }
            }
        }
    }

    private void updateDimensionBalance(FiscalPeriod period, Long companyId, JournalEntryLine line, String dimensionType, String dimensionCode, BigDecimal lineDebit, BigDecimal lineCredit) {
        if (dimensionCode == null || dimensionCode.isBlank()) {
            return;
        }
        DimensionBalance db = dimensionBalanceRepository.findBalance(
                period.getId(), dimensionType, dimensionCode, line.getAccount().getId()
        ).orElseGet(() -> {
            DimensionBalance newDb = new DimensionBalance();
            newDb.setAccount(line.getAccount());
            newDb.setDimensionType(dimensionType);
            newDb.setDimensionCode(dimensionCode);
            newDb.setFiscalPeriod(period);
            newDb.setFiscalYear(period.getFiscalYear());
            newDb.setBranch(line.getBranch());
            newDb.setOpeningBalance(BigDecimal.ZERO);
            newDb.setPeriodDebit(BigDecimal.ZERO);
            newDb.setPeriodCredit(BigDecimal.ZERO);
            newDb.setClosingBalance(BigDecimal.ZERO);
            newDb.setCompanyId(companyId);
            return newDb;
        });

        db.setPeriodDebit(db.getPeriodDebit().add(lineDebit));
        db.setPeriodCredit(db.getPeriodCredit().add(lineCredit));
        db.setClosingBalance(db.getOpeningBalance().add(db.getPeriodDebit()).subtract(db.getPeriodCredit()));

        dimensionBalanceRepository.save(db);
    }

    private void validateJournalEntry(JournalEntry journalEntry) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        Map<Long, BigDecimal> internalOrderNetSpend = new HashMap<>();

        if (journalEntry.getLines() != null) {
            for (JournalEntryLine line : journalEntry.getLines()) {
                BigDecimal lineDebit = line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO;
                BigDecimal lineCredit = line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO;
                totalDebit = totalDebit.add(lineDebit);
                totalCredit = totalCredit.add(lineCredit);
                validateDimensions(line);

                if (line.getInternalOrder() != null && line.getInternalOrder().getId() != null) {
                    Long ioId = line.getInternalOrder().getId();
                    BigDecimal netAmount = lineDebit.subtract(lineCredit);
                    internalOrderNetSpend.merge(ioId, netAmount, BigDecimal::add);
                }
            }
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new RuntimeException("Journal Entry is unbalanced. Debit: " + totalDebit + ", Credit: " + totalCredit);
        }

        // Validate Internal Order active status and budget limits
        boolean isReversal = journalEntry.getReferenceType() != null && journalEntry.getReferenceType().endsWith("_REVERSAL");
        for (Map.Entry<Long, BigDecimal> entry : internalOrderNetSpend.entrySet()) {
            InternalOrder io = internalOrderRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Internal Order not found with ID: " + entry.getKey()));

            if (!isReversal && Boolean.FALSE.equals(io.getActive())) {
                throw new RuntimeException("Internal Order " + io.getCode() + " is inactive and cannot be used for posting.");
            }

            BigDecimal attemptedSpend = entry.getValue();
            boolean budgetControlled = io.getBudgetControlled() == null || Boolean.TRUE.equals(io.getBudgetControlled());
            BigDecimal budget = io.getBudget() != null ? io.getBudget() : BigDecimal.ZERO;
            BigDecimal utilized = io.getUtilizedAmount() != null ? io.getUtilizedAmount() : BigDecimal.ZERO;

            if (!isReversal && budgetControlled && attemptedSpend.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal projectedUtilized = utilized.add(attemptedSpend);
                if (projectedUtilized.compareTo(budget) > 0) {
                    BigDecimal available = budget.subtract(utilized);
                    throw new RuntimeException(
                            "Budget exceeded for Internal Order " + io.getCode() + " (" + (io.getName() != null ? io.getName() : "") + "): " +
                            "Allocated budget is " + budget + ", utilized is " + utilized +
                            ", available is " + available + ", but this journal entry attempts to spend " + attemptedSpend + "."
                    );
                }
            }
        }

        journalEntry.setTotalDebit(totalDebit);
        journalEntry.setTotalCredit(totalCredit);
    }

    private void validateDimensions(JournalEntryLine line) {
        if (line.getAccount() != null && line.getAccount().getId() != null) {
            Account account = accountRepository.findById(line.getAccount().getId())
                    .orElse(line.getAccount());
            line.setAccount(account);

            if (account.isBusinessPartnerRequired() && (line.getBusinessPartner() == null || line.getBusinessPartner().getId() == null)) {
                throw new RuntimeException("Business Partner is required for account: " + account.getCode());
            }
            if (account.isCostCenterRequired() && (line.getCostCenter() == null || line.getCostCenter().getId() == null)) {
                throw new RuntimeException("Cost Center is required for account: " + account.getCode());
            }
            if (account.isProjectRequired() && (line.getProject() == null || line.getProject().getId() == null)) {
                throw new RuntimeException("Project is required for account: " + account.getCode());
            }
            if (account.isInternalOrderRequired() && (line.getInternalOrder() == null || line.getInternalOrder().getId() == null)) {
                throw new RuntimeException("Internal Order is required for account: " + account.getCode());
            }
            if (account.isLcRequired() && (line.getLetterOfCredit() == null || line.getLetterOfCredit().getId() == null)) {
                throw new RuntimeException("Letter of Credit is required for account: " + account.getCode());
            }
            if (account.isLoanRequired() && (line.getLoan() == null || line.getLoan().getId() == null)) {
                throw new RuntimeException("Loan is required for account: " + account.getCode());
            }
        }
    }

    @Transactional
    public void deleteById(Long id) {
        JournalEntry existing = journalEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Journal Entry not found"));
        if (existing.getStatus() == JournalStatus.POSTED || existing.getStatus() == JournalStatus.REVERSED) {
            throw new RuntimeException("Cannot delete a POSTED or REVERSED journal entry. Reverse it instead.");
        }
        journalEntryRepository.deleteById(id);
    }

    @Transactional
    public void reverseByReference(String referenceType, Long referenceId) {
        List<JournalEntry> postedJournals = journalEntryRepository.findByReferenceTypeAndReferenceIdAndStatus(
                referenceType, referenceId, JournalStatus.POSTED);

        for (JournalEntry original : postedJournals) {
            original.setStatus(JournalStatus.REVERSED);
            journalEntryRepository.save(original);

            JournalEntry reversal = new JournalEntry();
            reversal.setPostingDate(LocalDate.now());
            reversal.setReferenceType(referenceType + "_REVERSAL");
            reversal.setReferenceId(referenceId);
            reversal.setStatus(JournalStatus.POSTED);
            reversal.setCompanyId(original.getCompanyId());

            if (original.getLines() != null) {
                List<JournalEntryLine> reversalLines = original.getLines().stream().map(origLine -> {
                    JournalEntryLine revLine = new JournalEntryLine();
                    revLine.setJournalEntry(reversal);
                    revLine.setAccount(origLine.getAccount());
                    revLine.setDebit(origLine.getCredit() != null ? origLine.getCredit() : BigDecimal.ZERO);
                    revLine.setCredit(origLine.getDebit() != null ? origLine.getDebit() : BigDecimal.ZERO);
                    revLine.setBusinessPartner(origLine.getBusinessPartner());
                    revLine.setCostCenter(origLine.getCostCenter());
                    revLine.setProfitCenter(origLine.getProfitCenter());
                    revLine.setProject(origLine.getProject());
                    revLine.setInternalOrder(origLine.getInternalOrder());
                    revLine.setLetterOfCredit(origLine.getLetterOfCredit());
                    revLine.setLoan(origLine.getLoan());
                    revLine.setBranch(origLine.getBranch());
                    revLine.setCompanyId(origLine.getCompanyId());
                    return revLine;
                }).collect(Collectors.toList());
                reversal.setLines(reversalLines);
            }

            this.save(reversal);
        }
    }

    private JournalEntryDTO convertToDTO(JournalEntry entity) {
        JournalEntryDTO dto = new JournalEntryDTO();
        BeanUtils.copyProperties(entity, dto, "lines");
        if (entity.getLines() != null) {
            dto.setLines(entity.getLines().stream().map(line -> {
                JournalEntryLineDTO lineDto = new JournalEntryLineDTO();
                lineDto.setId(line.getId());
                lineDto.setDebit(line.getDebit());
                lineDto.setCredit(line.getCredit());

                if (line.getAccount() != null) {
                    AccountDTO accDto = new AccountDTO();
                    accDto.setId(line.getAccount().getId());
                    accDto.setCode(line.getAccount().getCode());
                    accDto.setName(line.getAccount().getName());
                    accDto.setAccountType(line.getAccount().getAccountType());
                    lineDto.setAccount(accDto);
                    lineDto.setAccountId(line.getAccount().getId());
                }
                if (line.getCostCenter() != null) {
                    CostCenterDTO ccDto = new CostCenterDTO();
                    ccDto.setId(line.getCostCenter().getId());
                    ccDto.setCode(line.getCostCenter().getCode());
                    ccDto.setName(line.getCostCenter().getName());
                    lineDto.setCostCenter(ccDto);
                    lineDto.setCostCenterId(line.getCostCenter().getId());
                }
                if (line.getProfitCenter() != null) {
                    ProfitCenterDTO pcDto = new ProfitCenterDTO();
                    pcDto.setId(line.getProfitCenter().getId());
                    pcDto.setCode(line.getProfitCenter().getCode());
                    pcDto.setName(line.getProfitCenter().getName());
                    lineDto.setProfitCenter(pcDto);
                    lineDto.setProfitCenterId(line.getProfitCenter().getId());
                }
                if (line.getBranch() != null) {
                    JournalEntryLineDTO.IdNameDTO branchDto = new JournalEntryLineDTO.IdNameDTO();
                    branchDto.setId(line.getBranch().getId());
                    branchDto.setCode(line.getBranch().getCode());
                    branchDto.setName(line.getBranch().getName());
                    lineDto.setBranch(branchDto);
                    lineDto.setBranchId(line.getBranch().getId());
                }
                if (line.getBusinessPartner() != null) {
                    JournalEntryLineDTO.IdNameDTO bpDto = new JournalEntryLineDTO.IdNameDTO();
                    bpDto.setId(line.getBusinessPartner().getId());
                    bpDto.setCode(line.getBusinessPartner().getCode());
                    bpDto.setName(line.getBusinessPartner().getName());
                    lineDto.setBusinessPartner(bpDto);
                    lineDto.setBusinessPartnerId(line.getBusinessPartner().getId());
                }
                if (line.getProject() != null) {
                    JournalEntryLineDTO.IdNameDTO projDto = new JournalEntryLineDTO.IdNameDTO();
                    projDto.setId(line.getProject().getId());
                    projDto.setCode(line.getProject().getCode());
                    projDto.setName(line.getProject().getName());
                    lineDto.setProject(projDto);
                    lineDto.setProjectId(line.getProject().getId());
                }
                if (line.getInternalOrder() != null) {
                    JournalEntryLineDTO.IdNameDTO ioDto = new JournalEntryLineDTO.IdNameDTO();
                    ioDto.setId(line.getInternalOrder().getId());
                    ioDto.setCode(line.getInternalOrder().getCode());
                    ioDto.setName(line.getInternalOrder().getName());
                    lineDto.setInternalOrder(ioDto);
                    lineDto.setInternalOrderId(line.getInternalOrder().getId());
                }
                if (line.getLetterOfCredit() != null) {
                    JournalEntryLineDTO.IdNameDTO lcDto = new JournalEntryLineDTO.IdNameDTO();
                    lcDto.setId(line.getLetterOfCredit().getId());
                    lcDto.setCode(line.getLetterOfCredit().getLcNumber());
                    lcDto.setName(line.getLetterOfCredit().getLcNumber());
                    lineDto.setLetterOfCredit(lcDto);
                    lineDto.setLetterOfCreditId(line.getLetterOfCredit().getId());
                }
                if (line.getLoan() != null) {
                    JournalEntryLineDTO.IdNameDTO loanDto = new JournalEntryLineDTO.IdNameDTO();
                    loanDto.setId(line.getLoan().getId());
                    loanDto.setCode(line.getLoan().getCode());
                    loanDto.setName(line.getLoan().getName());
                    lineDto.setLoan(loanDto);
                    lineDto.setLoanId(line.getLoan().getId());
                }
                return lineDto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private JournalEntry convertToEntity(JournalEntryDTO dto) {
        JournalEntry entity = new JournalEntry();
        BeanUtils.copyProperties(dto, entity, "lines");
        if (dto.getLines() != null) {
            entity.setLines(dto.getLines().stream().map(lineDto -> {
                JournalEntryLine line = new JournalEntryLine();
                line.setId(lineDto.getId());
                line.setDebit(lineDto.getDebit());
                line.setCredit(lineDto.getCredit());
                line.setJournalEntry(entity);

                Long accId = lineDto.getAccount() != null ? lineDto.getAccount().getId() : lineDto.getAccountId();
                if (accId != null) {
                    line.setAccount(entityManager.getReference(Account.class, accId));
                }
                Long ccId = lineDto.getCostCenter() != null ? lineDto.getCostCenter().getId() : lineDto.getCostCenterId();
                if (ccId != null) {
                    line.setCostCenter(entityManager.getReference(CostCenter.class, ccId));
                }
                Long pcId = lineDto.getProfitCenter() != null ? lineDto.getProfitCenter().getId() : lineDto.getProfitCenterId();
                if (pcId != null) {
                    line.setProfitCenter(entityManager.getReference(ProfitCenter.class, pcId));
                }
                Long brId = lineDto.getBranch() != null ? lineDto.getBranch().getId() : lineDto.getBranchId();
                if (brId != null) {
                    line.setBranch(entityManager.getReference(Branch.class, brId));
                }
                Long bpId = lineDto.getBusinessPartner() != null ? lineDto.getBusinessPartner().getId() : lineDto.getBusinessPartnerId();
                if (bpId != null) {
                    line.setBusinessPartner(entityManager.getReference(BusinessPartner.class, bpId));
                }
                Long projId = lineDto.getProject() != null ? lineDto.getProject().getId() : lineDto.getProjectId();
                if (projId != null) {
                    line.setProject(entityManager.getReference(Project.class, projId));
                }
                Long ioId = lineDto.getInternalOrder() != null ? lineDto.getInternalOrder().getId() : lineDto.getInternalOrderId();
                if (ioId != null) {
                    line.setInternalOrder(entityManager.getReference(InternalOrder.class, ioId));
                }
                Long lcId = lineDto.getLetterOfCredit() != null ? lineDto.getLetterOfCredit().getId() : lineDto.getLetterOfCreditId();
                if (lcId != null) {
                    line.setLetterOfCredit(entityManager.getReference(LetterOfCredit.class, lcId));
                }
                Long loanId = lineDto.getLoan() != null ? lineDto.getLoan().getId() : lineDto.getLoanId();
                if (loanId != null) {
                    line.setLoan(entityManager.getReference(Loan.class, loanId));
                }
                return line;
            }).collect(Collectors.toList()));
        }
        return entity;
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.JournalEntry> searchJournalEntrys(String query, org.springframework.data.domain.Pageable pageable) {
        return journalEntryRepository.findAll(org.enterprise.finance.specification.JournalEntrySpecification.searchByQuery(query), pageable);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.AccountBalance> searchAccountBalances(String query, org.springframework.data.domain.Pageable pageable) {
        return accountBalanceRepository.findAll(org.enterprise.finance.specification.AccountBalanceSpecification.searchByQuery(query), pageable);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.DimensionBalance> searchDimensionBalances(String query, org.springframework.data.domain.Pageable pageable) {
        return dimensionBalanceRepository.findAll(org.enterprise.finance.specification.DimensionBalanceSpecification.searchByQuery(query), pageable);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.FiscalPeriod> searchFiscalPeriods(String query, org.springframework.data.domain.Pageable pageable) {
        return fiscalPeriodRepository.findAll(org.enterprise.finance.specification.FiscalPeriodSpecification.searchByQuery(query), pageable);
    }
}
