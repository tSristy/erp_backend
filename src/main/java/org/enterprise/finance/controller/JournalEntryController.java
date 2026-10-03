package org.enterprise.finance.controller;

import org.enterprise.finance.dto.JournalEntryDTO;
import org.enterprise.finance.service.JournalEntryService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/journal-entrys")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    @GetMapping
    public ResponseEntity<List<JournalEntryDTO>> getAll() {
        return ResponseEntity.ok(journalEntryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalEntryDTO> getById(@PathVariable Long id) {
        JournalEntryDTO dto = journalEntryService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<JournalEntryDTO> create(@Valid @RequestBody JournalEntryDTO dto) {
        return ResponseEntity.ok(journalEntryService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JournalEntryDTO> update(@PathVariable Long id, @Valid @RequestBody JournalEntryDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(journalEntryService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        journalEntryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.JournalEntry> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return journalEntryService.searchJournalEntrys(q, pageable);
    }
}
