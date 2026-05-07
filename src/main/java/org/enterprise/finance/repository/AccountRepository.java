package org.enterprise.finance.repository;

import org.enterprise.finance.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByCode(String code);
    
    long countByAccountType(org.enterprise.finance.enums.AccountType accountType);
}
