package org.enterprise.finance.service;

import org.enterprise.finance.dto.JournalEntryDTO;
import org.enterprise.finance.entity.*;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.repository.AccountBalanceRepository;
import org.enterprise.finance.repository.DimensionBalanceRepository;
import org.enterprise.finance.repository.FiscalPeriodRepository;
import org.enterprise.finance.repository.JournalEntryRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final DimensionBalanceRepository dimensionBalanceRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;

    public JournalEntryService(JournalEntryRepository journalEntryRepository,
                               AccountBalanceRepository accountBalanceRepository,
                               DimensionBalanceRepository dimensionBalanceRepository,
                               FiscalPeriodRepository fiscalPeriodRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.accountBalanceRepository = accountBalanceRepository;
        this.dimensionBalanceRepository = dimensionBalanceRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
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
        }
        boolean transitioningToPosted = (existing != null && existing.getStatus() != JournalStatus.POSTED && journalEntry.getStatus() == JournalStatus.POSTED);

        validateJournalEntry(journalEntry);
        
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
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
                .orElseThrow(() -> new RuntimeException("No open fiscal period found for date: " + postingDate));

        for (JournalEntryLine line : journal.getLines()) {
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
                    newAb.setCompanyId(org.enterprise.common.util.TenantContext.getCompanyId());
                    return newAb;
                });

                BigDecimal lineDebit = line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO;
                BigDecimal lineCredit = line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO;

                ab.setPeriodDebit(ab.getPeriodDebit().add(lineDebit));
                ab.setPeriodCredit(ab.getPeriodCredit().add(lineCredit));

                ab.setClosingDebit(ab.getOpeningDebit().add(ab.getPeriodDebit()));
                ab.setClosingCredit(ab.getOpeningCredit().add(ab.getPeriodCredit()));

                accountBalanceRepository.save(ab);
            }

            // Update Sub-Ledger Balance for Business Partner
            if (line.getBusinessPartner() != null && line.getAccount() != null) {
                DimensionBalance bpb = dimensionBalanceRepository.findBalance(
                        period.getId(), "BUSINESS_PARTNER", line.getBusinessPartner().getCode(), line.getAccount().getId()
                ).orElseGet(() -> {
                    DimensionBalance newBpb = new DimensionBalance();
                    newBpb.setAccount(line.getAccount());
                    newBpb.setDimensionType("BUSINESS_PARTNER");
                    newBpb.setDimensionCode(line.getBusinessPartner().getCode());
                    newBpb.setFiscalPeriod(period);
                    newBpb.setFiscalYear(period.getFiscalYear());
                    newBpb.setBranch(line.getBranch());
                    newBpb.setOpeningBalance(BigDecimal.ZERO);
                    newBpb.setPeriodDebit(BigDecimal.ZERO);
                    newBpb.setPeriodCredit(BigDecimal.ZERO);
                    newBpb.setClosingBalance(BigDecimal.ZERO);
                    newBpb.setCompanyId(org.enterprise.common.util.TenantContext.getCompanyId());
                    return newBpb;
                });

                BigDecimal lineDebit = line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO;
                BigDecimal lineCredit = line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO;

                bpb.setPeriodDebit(bpb.getPeriodDebit().add(lineDebit));
                bpb.setPeriodCredit(bpb.getPeriodCredit().add(lineCredit));

                bpb.setClosingBalance(bpb.getOpeningBalance().add(bpb.getPeriodDebit()).subtract(bpb.getPeriodCredit()));

                dimensionBalanceRepository.save(bpb);
            }
        }
    }

    private void validateJournalEntry(JournalEntry journalEntry) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        if (journalEntry.getLines() != null) {
            for (JournalEntryLine line : journalEntry.getLines()) {
                totalDebit = totalDebit.add(line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO);
                totalCredit = totalCredit.add(line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO);
                validateDimensions(line);
            }
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new RuntimeException("Journal Entry is unbalanced. Debit: " + totalDebit + ", Credit: " + totalCredit);
        }

        journalEntry.setTotalDebit(totalDebit);
        journalEntry.setTotalCredit(totalCredit);
    }

    private void validateDimensions(JournalEntryLine line) {
        if (line.getAccount() != null) {
            Account account = line.getAccount();
            if (account.isBusinessPartnerRequired() && line.getBusinessPartner() == null) {
                throw new RuntimeException("Business Partner is required for account: " + account.getCode());
            }
            if (account.isCostCenterRequired() && line.getCostCenter() == null) {
                throw new RuntimeException("Cost Center is required for account: " + account.getCode());
            }
            if (account.isProjectRequired() && line.getProject() == null) {
                throw new RuntimeException("Project is required for account: " + account.getCode());
            }
            if (account.isLcRequired() && line.getLetterOfCredit() == null) {
                throw new RuntimeException("Letter of Credit is required for account: " + account.getCode());
            }
            if (account.isLoanRequired() && line.getLoan() == null) {
                throw new RuntimeException("Loan is required for account: " + account.getCode());
            }
        }
    }

    @Transactional
    public void deleteById(Long id) {
        journalEntryRepository.deleteById(id);
    }

    private JournalEntryDTO convertToDTO(JournalEntry entity) {
        JournalEntryDTO dto = new JournalEntryDTO();
        BeanUtils.copyProperties(entity, dto, "lines");
        if (entity.getLines() != null) {
            dto.setLines(entity.getLines().stream().map(line -> {
                org.enterprise.finance.dto.JournalEntryLineDTO lineDto = new org.enterprise.finance.dto.JournalEntryLineDTO();
                BeanUtils.copyProperties(line, lineDto);
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
                org.enterprise.finance.entity.JournalEntryLine line = new org.enterprise.finance.entity.JournalEntryLine();
                BeanUtils.copyProperties(lineDto, line);
                line.setJournalEntry(entity);
                return line;
            }).collect(Collectors.toList()));
        }
        return entity;
    }
}
