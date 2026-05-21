package org.enterprise.finance.controller;

import org.enterprise.finance.dto.JournalEntryDTO;
import org.enterprise.finance.service.JournalEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/journal-entrys")
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
    public ResponseEntity<JournalEntryDTO> create(@RequestBody JournalEntryDTO dto) {
        return ResponseEntity.ok(journalEntryService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JournalEntryDTO> update(@PathVariable Long id, @RequestBody JournalEntryDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(journalEntryService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        journalEntryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
