package org.enterprise.finance.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.entity.Account;
import org.enterprise.finance.enums.AccountType;
import org.enterprise.finance.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Generate Account Code based on AccountType
        String generatedCode = generateAccountCode(account.getAccountType());
        account.setCode(generatedCode);

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

        return accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        Account account = getAccountById(id);
        accountRepository.delete(account);
    }

    private String generateAccountCode(AccountType accountType) {
        long count = accountRepository.countByAccountType(accountType);
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
}
