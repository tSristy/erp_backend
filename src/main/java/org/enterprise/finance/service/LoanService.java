package org.enterprise.finance.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.finance.entity.Loan;
import org.enterprise.finance.repository.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;

    public Page<Loan> searchLoans(String search, int page, int size) {
        Long companyId = TenantContext.getCompanyId();
        Pageable pageable = PageRequest.of(page, size);
        return loanRepository.searchByCompanyId(companyId, search == null ? "" : search, pageable);
    }

    public Loan getLoanById(Long id) {
        Long companyId = TenantContext.getCompanyId();
        return loanRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
    }

    @Transactional
    public Loan createLoan(Loan loan) {
        Long companyId = TenantContext.getCompanyId();
        
        if (loan.getCode() == null || loan.getCode().trim().isEmpty()) {
            String maxCode = loanRepository.findMaxCodeByCompanyId(companyId).orElse(null);
            int nextNum = 1;
            if (maxCode != null && maxCode.matches("^LOAN-\\d{6}$")) {
                try {
                    nextNum = Integer.parseInt(maxCode.substring(5)) + 1;
                } catch (NumberFormatException ignored) {}
            }
            loan.setCode(String.format("LOAN-%06d", nextNum));
        } else {
            if (!loan.getCode().matches("^[A-Z0-9]+-\\d{6}$")) {
                throw new RuntimeException("Loan code must have a prefix followed by 6 digits (e.g., LOAN-000001)");
            }
            loanRepository.findByCodeAndCompanyId(loan.getCode(), companyId)
                    .ifPresent(p -> {
                        throw new RuntimeException("Loan code already exists: " + loan.getCode());
                    });
        }
        
        loan.setCompanyId(companyId);
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan updateLoan(Long id, Loan details) {
        Loan loan = getLoanById(id);
        
        if (details.getCode() != null && !details.getCode().equals(loan.getCode())) {
            if (!details.getCode().matches("^[A-Z0-9]+-\\d{6}$")) {
                throw new RuntimeException("Loan code must have a prefix followed by 6 digits (e.g., LOAN-000001)");
            }
            loanRepository.findByCodeAndCompanyId(details.getCode(), TenantContext.getCompanyId())
                    .ifPresent(p -> {
                        throw new RuntimeException("Loan code already exists: " + details.getCode());
                    });
            loan.setCode(details.getCode());
        }

        loan.setName(details.getName());
        loan.setPrincipalAmount(details.getPrincipalAmount());
        loan.setInterestRate(details.getInterestRate());
        if (details.getActive() != null) {
            loan.setActive(details.getActive());
        }

        return loanRepository.save(loan);
    }

    @Transactional
    public void deleteLoan(Long id) {
        Loan loan = getLoanById(id);
        loanRepository.delete(loan);
    }
}
