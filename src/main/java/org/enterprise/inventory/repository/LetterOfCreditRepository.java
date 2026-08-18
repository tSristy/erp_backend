package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.LetterOfCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LetterOfCreditRepository extends JpaRepository<LetterOfCredit, Long> {
    List<LetterOfCredit> findByCompanyId(Long companyId);
}
