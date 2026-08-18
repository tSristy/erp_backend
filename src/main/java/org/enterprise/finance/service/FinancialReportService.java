package org.enterprise.finance.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.finance.dto.LedgerRowDto;
import org.enterprise.finance.dto.TrialBalanceRowDto;
import org.enterprise.finance.entity.AccountBalance;
import org.enterprise.finance.entity.FiscalPeriod;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.repository.AccountBalanceRepository;
import org.enterprise.finance.repository.DimensionBalanceRepository;
import org.enterprise.finance.repository.FiscalPeriodRepository;
import org.enterprise.finance.repository.JournalEntryLineRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialReportService {

    private final AccountBalanceRepository accountBalanceRepository;
    private final DimensionBalanceRepository dimensionBalanceRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;

    public List<TrialBalanceRowDto> getTrialBalance(Long periodId) {
        Long companyId = TenantContext.getCompanyId();
        List<AccountBalance> balances = accountBalanceRepository.findByFiscalPeriodIdAndCompanyId(periodId, companyId);
        
        return balances.stream().map(b -> {
            TrialBalanceRowDto dto = new TrialBalanceRowDto();
            dto.setAccountId(b.getAccount().getId());
            dto.setAccountCode(b.getAccount().getCode());
            dto.setAccountName(b.getAccount().getName());
            dto.setOpeningDebit(b.getOpeningDebit() != null ? b.getOpeningDebit() : BigDecimal.ZERO);
            dto.setOpeningCredit(b.getOpeningCredit() != null ? b.getOpeningCredit() : BigDecimal.ZERO);
            dto.setPeriodDebit(b.getPeriodDebit() != null ? b.getPeriodDebit() : BigDecimal.ZERO);
            dto.setPeriodCredit(b.getPeriodCredit() != null ? b.getPeriodCredit() : BigDecimal.ZERO);
            dto.setClosingDebit(b.getClosingDebit() != null ? b.getClosingDebit() : BigDecimal.ZERO);
            dto.setClosingCredit(b.getClosingCredit() != null ? b.getClosingCredit() : BigDecimal.ZERO);
            return dto;
        }).collect(Collectors.toList());
    }

    public List<LedgerRowDto> getGlLedger(String glCode, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        Long companyId = TenantContext.getCompanyId();
        
        FiscalPeriod period = fiscalPeriodRepository.findPeriodByDate(startDate).orElse(null);
        BigDecimal baseOpening = BigDecimal.ZERO;
        
        if (period != null) {
            List<AccountBalance> balances = accountBalanceRepository.findByFiscalPeriodIdAndCompanyId(period.getId(), companyId);
            AccountBalance balance = balances.stream()
                .filter(b -> glCode.equals(b.getAccount().getCode()))
                .findFirst()
                .orElse(null);
            
            if (balance != null) {
                BigDecimal openDebit = balance.getOpeningDebit() != null ? balance.getOpeningDebit() : BigDecimal.ZERO;
                BigDecimal openCredit = balance.getOpeningCredit() != null ? balance.getOpeningCredit() : BigDecimal.ZERO;
                baseOpening = openDebit.subtract(openCredit);
            }
            
            if (startDate.isAfter(period.getStartDate())) {
                BigDecimal rollForward = journalEntryLineRepository.calculateAccountRollForwardBalance(glCode, period.getStartDate(), startDate);
                if (rollForward != null) {
                    baseOpening = baseOpening.add(rollForward);
                }
            }
        } else {
            // No fiscal period found containing the start date, calculate purely based on roll forward from a very old date or assume 0 base.
            // Assuming 0 base for simplicity if periods aren't configured properly.
            BigDecimal rollForward = journalEntryLineRepository.calculateAccountRollForwardBalance(glCode, java.time.LocalDate.of(2000, 1, 1), startDate);
            if (rollForward != null) {
                baseOpening = baseOpening.add(rollForward);
            }
        }
            
        List<LedgerRowDto> result = new ArrayList<>();
        
        BigDecimal currentBalance = BigDecimal.ZERO;
        
        LedgerRowDto opening = new LedgerRowDto();
        opening.setDate(startDate);
        opening.setReference("Opening Balance");
        opening.setDescription("Opening Balance");
        
        if (baseOpening.compareTo(BigDecimal.ZERO) >= 0) {
            opening.setDebit(baseOpening);
            opening.setCredit(BigDecimal.ZERO);
        } else {
            opening.setDebit(BigDecimal.ZERO);
            opening.setCredit(baseOpening.abs());
        }
        
        currentBalance = baseOpening;
        opening.setBalance(currentBalance);
        result.add(opening);
        
        List<JournalEntryLine> lines = journalEntryLineRepository.findByAccountCodeAndDateRange(glCode, startDate, endDate);
        
        BigDecimal periodDebitTotal = BigDecimal.ZERO;
        BigDecimal periodCreditTotal = BigDecimal.ZERO;
        
        for (JournalEntryLine line : lines) {
            LedgerRowDto row = new LedgerRowDto();
            row.setDate(line.getJournalEntry().getPostingDate());
            row.setReference(line.getJournalEntry().getReferenceType() + "-" + line.getJournalEntry().getReferenceId());
            row.setDescription(line.getJournalEntry().getJournalNo() != null ? line.getJournalEntry().getJournalNo() : "");
            
            BigDecimal debit = line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO;
            
            periodDebitTotal = periodDebitTotal.add(debit);
            periodCreditTotal = periodCreditTotal.add(credit);
            
            row.setDebit(debit);
            row.setCredit(credit);
            
            currentBalance = currentBalance.add(debit).subtract(credit);
            row.setBalance(currentBalance);
            
            result.add(row);
        }
        
        LedgerRowDto closing = new LedgerRowDto();
        closing.setDate(endDate);
        closing.setReference("Closing Balance");
        closing.setDescription("Closing Balance");
        
        if (currentBalance.compareTo(BigDecimal.ZERO) >= 0) {
            closing.setDebit(currentBalance);
            closing.setCredit(BigDecimal.ZERO);
        } else {
            closing.setDebit(BigDecimal.ZERO);
            closing.setCredit(currentBalance.abs());
        }
        
        closing.setBalance(currentBalance);
        result.add(closing);
        
        return result;
    }

    public List<LedgerRowDto> getCustomerLedger(String customerCode, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        Long companyId = TenantContext.getCompanyId();
        
        FiscalPeriod period = fiscalPeriodRepository.findPeriodByDate(startDate).orElse(null);
        BigDecimal baseOpening = BigDecimal.ZERO;
        
        if (period != null) {
            BigDecimal totalOpening = dimensionBalanceRepository.calculateTotalDimensionBalance(period.getId(), "CUSTOMER", customerCode);
            if (totalOpening != null) {
                baseOpening = totalOpening;
            }
            
            if (startDate.isAfter(period.getStartDate())) {
                BigDecimal rollForward = journalEntryLineRepository.calculateBusinessPartnerRollForwardBalance(customerCode, period.getStartDate(), startDate);
                if (rollForward != null) {
                    baseOpening = baseOpening.add(rollForward);
                }
            }
        } else {
            BigDecimal rollForward = journalEntryLineRepository.calculateBusinessPartnerRollForwardBalance(customerCode, java.time.LocalDate.of(2000, 1, 1), startDate);
            if (rollForward != null) {
                baseOpening = baseOpening.add(rollForward);
            }
        }
            
        List<LedgerRowDto> result = new ArrayList<>();
        
        BigDecimal currentBalance = baseOpening;
        
        LedgerRowDto opening = new LedgerRowDto();
        opening.setDate(startDate);
        opening.setReference("Opening Balance");
        opening.setDescription("Opening Balance");
        
        if (currentBalance.compareTo(BigDecimal.ZERO) >= 0) {
            opening.setDebit(currentBalance);
            opening.setCredit(BigDecimal.ZERO);
        } else {
            opening.setDebit(BigDecimal.ZERO);
            opening.setCredit(currentBalance.abs());
        }
        
        opening.setBalance(currentBalance);
        result.add(opening);
        
        List<JournalEntryLine> lines = journalEntryLineRepository.findByBusinessPartnerCodeAndDateRange(customerCode, startDate, endDate);
        
        for (JournalEntryLine line : lines) {
            LedgerRowDto row = new LedgerRowDto();
            row.setDate(line.getJournalEntry().getPostingDate());
            row.setReference(line.getJournalEntry().getReferenceType() + "-" + line.getJournalEntry().getReferenceId());
            row.setDescription(line.getJournalEntry().getJournalNo() != null ? line.getJournalEntry().getJournalNo() : "");
            
            BigDecimal debit = line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO;
            
            row.setDebit(debit);
            row.setCredit(credit);
            
            currentBalance = currentBalance.add(debit).subtract(credit);
            row.setBalance(currentBalance);
            
            result.add(row);
        }
        
        LedgerRowDto closing = new LedgerRowDto();
        closing.setDate(endDate);
        closing.setReference("Closing Balance");
        closing.setDescription("Closing Balance");
        
        if (currentBalance.compareTo(BigDecimal.ZERO) >= 0) {
            closing.setDebit(currentBalance);
            closing.setCredit(BigDecimal.ZERO);
        } else {
            closing.setDebit(BigDecimal.ZERO);
            closing.setCredit(currentBalance.abs());
        }
        
        closing.setBalance(currentBalance);
        result.add(closing);
        
        return result;
    }

    public List<LedgerRowDto> getVendorLedger(String vendorCode, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        Long companyId = TenantContext.getCompanyId();
        
        FiscalPeriod period = fiscalPeriodRepository.findPeriodByDate(startDate).orElse(null);
        BigDecimal baseOpening = BigDecimal.ZERO;
        
        if (period != null) {
            BigDecimal totalOpening = dimensionBalanceRepository.calculateTotalDimensionBalance(period.getId(), "VENDOR", vendorCode);
            if (totalOpening != null) {
                baseOpening = totalOpening;
            }
            
            if (startDate.isAfter(period.getStartDate())) {
                BigDecimal rollForward = journalEntryLineRepository.calculateBusinessPartnerRollForwardBalance(vendorCode, period.getStartDate(), startDate);
                if (rollForward != null) {
                    baseOpening = baseOpening.add(rollForward);
                }
            }
        } else {
            BigDecimal rollForward = journalEntryLineRepository.calculateBusinessPartnerRollForwardBalance(vendorCode, java.time.LocalDate.of(2000, 1, 1), startDate);
            if (rollForward != null) {
                baseOpening = baseOpening.add(rollForward);
            }
        }
            
        List<LedgerRowDto> result = new ArrayList<>();
        
        // Vendor balance is credit-based usually, but currentBalance represents (Debit - Credit) naturally
        // If it's a vendor, baseOpening is probably negative if they are owed money.
        BigDecimal currentBalance = baseOpening;
        
        LedgerRowDto opening = new LedgerRowDto();
        opening.setDate(startDate);
        opening.setReference("Opening Balance");
        opening.setDescription("Opening Balance");
        
        if (currentBalance.compareTo(BigDecimal.ZERO) >= 0) {
            opening.setDebit(currentBalance);
            opening.setCredit(BigDecimal.ZERO);
        } else {
            opening.setDebit(BigDecimal.ZERO);
            opening.setCredit(currentBalance.abs());
        }
        
        // For display logic, we might want to show vendor balances as positive if it's a credit balance,
        // but to keep consistency, we'll maintain balance = debit - credit or vice versa depending on frontend.
        // Usually, Vendor ledger balance is shown as absolute or negated. Let's negate it so a payable is positive.
        opening.setBalance(currentBalance.negate());
        result.add(opening);
        
        List<JournalEntryLine> lines = journalEntryLineRepository.findByBusinessPartnerCodeAndDateRange(vendorCode, startDate, endDate);
        
        for (JournalEntryLine line : lines) {
            LedgerRowDto row = new LedgerRowDto();
            row.setDate(line.getJournalEntry().getPostingDate());
            row.setReference(line.getJournalEntry().getReferenceType() + "-" + line.getJournalEntry().getReferenceId());
            row.setDescription(line.getJournalEntry().getJournalNo() != null ? line.getJournalEntry().getJournalNo() : "");
            
            BigDecimal debit = line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO;
            
            row.setDebit(debit);
            row.setCredit(credit);
            
            currentBalance = currentBalance.add(debit).subtract(credit);
            row.setBalance(currentBalance.negate());
            
            result.add(row);
        }
        
        LedgerRowDto closing = new LedgerRowDto();
        closing.setDate(endDate);
        closing.setReference("Closing Balance");
        closing.setDescription("Closing Balance");
        
        if (currentBalance.compareTo(BigDecimal.ZERO) >= 0) {
            closing.setDebit(currentBalance);
            closing.setCredit(BigDecimal.ZERO);
        } else {
            closing.setDebit(BigDecimal.ZERO);
            closing.setCredit(currentBalance.abs());
        }
        
        closing.setBalance(currentBalance.negate());
        result.add(closing);
        
        return result;
    }

    public List<LedgerRowDto> getDimensionLedger(String dimensionType, String dimensionCode, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        // We do not track pre-calculated Fiscal Period balances for arbitrary dimensions yet.
        // Therefore, we calculate the Opening Balance by summing all transactions from the beginning of time up to startDate.
        
        // 1. Calculate Opening Balance
        BigDecimal baseOpening = BigDecimal.ZERO;
        
        boolean isBpDimension = dimensionType.equalsIgnoreCase("EMPLOYEE") || 
                               dimensionType.equalsIgnoreCase("CUSTOMER") || 
                               dimensionType.equalsIgnoreCase("VENDOR") || 
                               dimensionType.equalsIgnoreCase("BUSINESS_PARTNER");

        if (isBpDimension) {
            FiscalPeriod period = fiscalPeriodRepository.findPeriodByDate(startDate).orElse(null);
            
            if (period != null) {
                // If they want the balance irrespective of GL, calculate total dimension balance:
                BigDecimal totalOpening = dimensionBalanceRepository.calculateTotalDimensionBalance(period.getId(), dimensionType, dimensionCode);
                if (totalOpening != null) {
                    baseOpening = totalOpening;
                }
                
                if (startDate.isAfter(period.getStartDate())) {
                    BigDecimal rollForward = journalEntryLineRepository.calculateBusinessPartnerRollForwardBalance(dimensionCode, period.getStartDate(), startDate);
                    if (rollForward != null) {
                        baseOpening = baseOpening.add(rollForward);
                    }
                }
            } else {
                BigDecimal rollForward = journalEntryLineRepository.calculateBusinessPartnerRollForwardBalance(dimensionCode, java.time.LocalDate.of(2000, 1, 1), startDate);
                if (rollForward != null) {
                    baseOpening = baseOpening.add(rollForward);
                }
            }
        } else {
            // For Cost Center, Project, Loan, etc. we don't have pre-calculated balance tables yet.
            // Calculate by summing from beginning of time up to startDate.
            org.springframework.data.jpa.domain.Specification<JournalEntryLine> openingSpec = 
                org.enterprise.finance.repository.JournalEntryLineSpecification.calculateRollForward(dimensionType, dimensionCode, startDate);
                
            List<JournalEntryLine> openingLines = journalEntryLineRepository.findAll(openingSpec);
            
            for (JournalEntryLine line : openingLines) {
                BigDecimal debit = line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO;
                BigDecimal credit = line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO;
                baseOpening = baseOpening.add(debit).subtract(credit);
            }
        }

        List<LedgerRowDto> result = new ArrayList<>();
        BigDecimal currentBalance = baseOpening;

        LedgerRowDto opening = new LedgerRowDto();
        opening.setDate(startDate);
        opening.setReference("Opening Balance");
        opening.setDescription("Opening Balance");

        if (currentBalance.compareTo(BigDecimal.ZERO) >= 0) {
            opening.setDebit(currentBalance);
            opening.setCredit(BigDecimal.ZERO);
        } else {
            opening.setDebit(BigDecimal.ZERO);
            opening.setCredit(currentBalance.abs());
        }
        
        // Some UI logic might prefer Vendor to be negated, but Subledger standardizes on Debit-Positive
        opening.setBalance(currentBalance);
        result.add(opening);

        // 2. Fetch Period Lines
        org.springframework.data.jpa.domain.Specification<JournalEntryLine> periodSpec = 
            org.enterprise.finance.repository.JournalEntryLineSpecification.byDimensionAndDateRange(dimensionType, dimensionCode, startDate, endDate);
            
        List<JournalEntryLine> lines = journalEntryLineRepository.findAll(periodSpec);

        for (JournalEntryLine line : lines) {
            LedgerRowDto row = new LedgerRowDto();
            row.setDate(line.getJournalEntry().getPostingDate());
            row.setReference(line.getJournalEntry().getReferenceType() + "-" + line.getJournalEntry().getReferenceId());
            row.setDescription(line.getJournalEntry().getJournalNo() != null ? line.getJournalEntry().getJournalNo() : "");

            BigDecimal debit = line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO;

            row.setDebit(debit);
            row.setCredit(credit);

            currentBalance = currentBalance.add(debit).subtract(credit);
            row.setBalance(currentBalance);

            result.add(row);
        }

        LedgerRowDto closing = new LedgerRowDto();
        closing.setDate(endDate);
        closing.setReference("Closing Balance");
        closing.setDescription("Closing Balance");

        if (currentBalance.compareTo(BigDecimal.ZERO) >= 0) {
            closing.setDebit(currentBalance);
            closing.setCredit(BigDecimal.ZERO);
        } else {
            closing.setDebit(BigDecimal.ZERO);
            closing.setCredit(currentBalance.abs());
        }

        closing.setBalance(currentBalance);
        result.add(closing);

        return result;
    }
}
