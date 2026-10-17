package org.enterprise.finance.repository;

import org.enterprise.finance.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long>, JpaSpecificationExecutor<JournalEntry> {
    long countByCompanyId(Long companyId);

    java.util.List<JournalEntry> findByReferenceTypeAndReferenceIdAndStatus(
            String referenceType,
            Long referenceId,
            org.enterprise.finance.enums.JournalStatus status
    );
}
