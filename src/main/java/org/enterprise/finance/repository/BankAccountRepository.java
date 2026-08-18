package org.enterprise.finance.repository;

import org.enterprise.finance.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByCompanyId(Long companyId);
    long countByCompanyId(Long companyId);
}
