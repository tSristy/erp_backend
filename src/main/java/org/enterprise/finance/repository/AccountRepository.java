package org.enterprise.finance.repository;

import org.enterprise.finance.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByCode(String code);
    Account findByCodeAndCompanyId(String code, Long companyId);
    long countByCompanyId(Long companyId);
    
    long countByAccountTypeAndCompanyId(org.enterprise.finance.enums.AccountType accountType, Long companyId);
}
