package org.enterprise.finance.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.entity.Account;
import org.enterprise.finance.enums.AccountType;
import org.enterprise.finance.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.enterprise.common.util.TenantContext;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + id));
    }

    @Transactional
    public Account createAccount(Account account) {
        if (account.getAccountType() == null) {
            throw new IllegalArgumentException("AccountType is required");
        }

        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new RuntimeException("No active company context");
        }
        
        account.setCompanyId(companyId);

        if (account.getCode() != null && !account.getCode().trim().isEmpty()) {
            validateAccountCodeRange(account.getCode(), account.getAccountType());
        } else {
            // Generate Account Code based on AccountType
            String generatedCode = generateAccountCode(account.getAccountType(), companyId);
            account.setCode(generatedCode);
        }

        return accountRepository.save(account);
    }

    @Transactional
    public Account updateAccount(Long id, Account accountDetails) {
        Account account = getAccountById(id);
        
        // We generally do not allow changing the accountType or code once created
        account.setName(accountDetails.getName());
        account.setAllowPosting(accountDetails.getAllowPosting());
        account.setActive(accountDetails.getActive());
        
        if (accountDetails.getParent() != null && accountDetails.getParent().getId() != null) {
            Account parent = getAccountById(accountDetails.getParent().getId());
            account.setParent(parent);
        }
        
        if (accountDetails.getCode() != null && !accountDetails.getCode().trim().isEmpty() && !accountDetails.getCode().equals(account.getCode())) {
            validateAccountCodeRange(accountDetails.getCode(), account.getAccountType());
            account.setCode(accountDetails.getCode());
        }

        return accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        Account account = getAccountById(id);
        accountRepository.delete(account);
    }

    private String generateAccountCode(AccountType accountType, Long companyId) {
        long count = accountRepository.countByAccountTypeAndCompanyId(accountType, companyId);
        long nextNumber = count + 1;

        int prefix;
        switch (accountType) {
            case ASSET:
                prefix = 1;
                break;
            case LIABILITY:
                prefix = 2;
                break;
            case EQUITY:
                prefix = 3;
                break;
            case INCOME:
                prefix = 4;
                break;
            case EXPENSE:
                prefix = 5;
                break;
            default:
                throw new IllegalArgumentException("Unsupported AccountType: " + accountType);
        }

        // e.g., 10001, 10002, 20001
        return String.format("%d%04d", prefix, nextNumber);
    }
    
    private void validateAccountCodeRange(String code, AccountType type) {
        try {
            int codeValue = Integer.parseInt(code);
            int min, max;
            switch (type) {
                case ASSET: min = 10000; max = 19999; break;
                case LIABILITY: min = 20000; max = 29999; break;
                case EQUITY: min = 30000; max = 39999; break;
                case INCOME: min = 40000; max = 49999; break;
                case EXPENSE: min = 50000; max = 59999; break;
                default: throw new IllegalArgumentException("Unsupported AccountType: " + type);
            }
            if (codeValue < min || codeValue > max) {
                throw new IllegalArgumentException(String.format("Account code for %s must be between %d and %d", type, min, max));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Account code must be a numeric value");
        }
    }
}
