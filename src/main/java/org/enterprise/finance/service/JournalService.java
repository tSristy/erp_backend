package org.enterprise.finance.service;

import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.finance.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalEntryRepository journalEntryRepository;

    @Transactional
    public JournalEntry save(JournalEntry journalEntry) {
        validateJournalEntry(journalEntry);
        return journalEntryRepository.save(journalEntry);
    }

    private void validateJournalEntry(JournalEntry journalEntry) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        if (journalEntry.getLines() != null) {
            for (JournalEntryLine line : journalEntry.getLines()) {
                totalDebit = totalDebit.add(line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO);
                totalCredit = totalCredit.add(line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO);
            }
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new RuntimeException("Journal Entry is unbalanced. Debit: " + totalDebit + ", Credit: " + totalCredit);
        }

        journalEntry.setTotalDebit(totalDebit);
        journalEntry.setTotalCredit(totalCredit);
    }
}
