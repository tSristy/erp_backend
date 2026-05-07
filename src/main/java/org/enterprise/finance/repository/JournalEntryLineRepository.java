package org.enterprise.finance.repository;

import org.enterprise.finance.entity.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {
}
