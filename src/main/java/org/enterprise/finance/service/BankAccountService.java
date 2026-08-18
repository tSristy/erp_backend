package org.enterprise.finance.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.finance.entity.BankAccount;
import org.enterprise.finance.repository.BankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    public List<BankAccount> getAllBankAccounts() {
        return bankAccountRepository.findAll();
    }

    public BankAccount getBankAccountById(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BankAccount not found with ID: " + id));
    }

    @Transactional
    public BankAccount createBankAccount(BankAccount bankAccount) {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new RuntimeException("No active company context");
        }
        
        bankAccount.setCompanyId(companyId);
        return bankAccountRepository.save(bankAccount);
    }

    @Transactional
    public BankAccount updateBankAccount(Long id, BankAccount details) {
        BankAccount existing = getBankAccountById(id);
        
        existing.setBankName(details.getBankName());
        existing.setBranchName(details.getBranchName());
        existing.setAccountNumber(details.getAccountNumber());
        existing.setRoutingNumber(details.getRoutingNumber());
        existing.setCurrencyCode(details.getCurrencyCode());
        existing.setActive(details.getActive());
        existing.setAccount(details.getAccount());

        return bankAccountRepository.save(existing);
    }

    @Transactional
    public void deleteBankAccount(Long id) {
        BankAccount bankAccount = getBankAccountById(id);
        bankAccountRepository.delete(bankAccount);
    }
}
