package org.enterprise.finance.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.finance.dto.FinanceDashboardDTO;
import org.enterprise.finance.repository.AccountRepository;
import org.enterprise.finance.repository.CostCenterRepository;
import org.enterprise.finance.repository.JournalEntryRepository;
import org.enterprise.finance.repository.ProfitCenterRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinanceDashboardService {

    private final AccountRepository accountRepository;
    private final CostCenterRepository costCenterRepository;
    private final ProfitCenterRepository profitCenterRepository;
    private final JournalEntryRepository journalEntryRepository;

    public FinanceDashboardDTO getDashboardMetrics() {
        Long companyId = TenantContext.get().getCompanyId();

        long accounts = accountRepository.countByCompanyId(companyId);
        long costCenters = costCenterRepository.countByCompanyId(companyId);
        long profitCenters = profitCenterRepository.countByCompanyId(companyId);
        long journals = journalEntryRepository.countByCompanyId(companyId);

        FinanceDashboardDTO dto = new FinanceDashboardDTO();
        dto.setTotalAccounts(accounts);
        dto.setTotalCostCenters(costCenters);
        dto.setTotalProfitCenters(profitCenters);
        dto.setTotalJournalEntries(journals);

        return dto;
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.Account> searchAccounts(String query, org.springframework.data.domain.Pageable pageable) {
        return accountRepository.findAll(org.enterprise.finance.specification.AccountSpecification.searchByQuery(query), pageable);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.CostCenter> searchCostCenters(String query, org.springframework.data.domain.Pageable pageable) {
        return costCenterRepository.findAll(org.enterprise.finance.specification.CostCenterSpecification.searchByQuery(query), pageable);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.ProfitCenter> searchProfitCenters(String query, org.springframework.data.domain.Pageable pageable) {
        return profitCenterRepository.findAll(org.enterprise.finance.specification.ProfitCenterSpecification.searchByQuery(query), pageable);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.JournalEntry> searchJournalEntrys(String query, org.springframework.data.domain.Pageable pageable) {
        return journalEntryRepository.findAll(org.enterprise.finance.specification.JournalEntrySpecification.searchByQuery(query), pageable);
    }
}
