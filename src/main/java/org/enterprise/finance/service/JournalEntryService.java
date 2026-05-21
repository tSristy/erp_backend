package org.enterprise.finance.service;

import org.enterprise.finance.dto.JournalEntryDTO;
import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.repository.JournalEntryRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.enterprise.finance.entity.JournalEntryLine;
import java.math.BigDecimal;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    public JournalEntryService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Transactional(readOnly = true)
    public List<JournalEntryDTO> findAll() {
        return journalEntryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JournalEntryDTO findById(Long id) {
        return journalEntryRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Transactional
    public JournalEntryDTO save(JournalEntryDTO dto) {
        JournalEntry entity = convertToEntity(dto);
        validateJournalEntry(entity);
        JournalEntry saved = journalEntryRepository.save(entity);
        return convertToDTO(saved);
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

    @Transactional
    public void deleteById(Long id) {
        journalEntryRepository.deleteById(id);
    }

    private JournalEntryDTO convertToDTO(JournalEntry entity) {
        JournalEntryDTO dto = new JournalEntryDTO();
        BeanUtils.copyProperties(entity, dto, "lines");
        if (entity.getLines() != null) {
            dto.setLines(entity.getLines().stream().map(line -> {
                org.enterprise.finance.dto.JournalEntryLineDTO lineDto = new org.enterprise.finance.dto.JournalEntryLineDTO();
                BeanUtils.copyProperties(line, lineDto);
                return lineDto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private JournalEntry convertToEntity(JournalEntryDTO dto) {
        JournalEntry entity = new JournalEntry();
        BeanUtils.copyProperties(dto, entity, "lines");
        if (dto.getLines() != null) {
            entity.setLines(dto.getLines().stream().map(lineDto -> {
                org.enterprise.finance.entity.JournalEntryLine line = new org.enterprise.finance.entity.JournalEntryLine();
                BeanUtils.copyProperties(lineDto, line);
                line.setJournalEntry(entity);
                return line;
            }).collect(Collectors.toList()));
        }
        return entity;
    }
}
