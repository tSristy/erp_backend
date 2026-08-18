package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.LetterOfCredit;
import org.enterprise.inventory.service.LetterOfCreditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/letters-of-credit")
@RequiredArgsConstructor
public class LetterOfCreditController {

    private final LetterOfCreditService letterOfCreditService;

    @GetMapping
    public ResponseEntity<List<LetterOfCredit>> getAllLCs() {
        return ResponseEntity.ok(letterOfCreditService.getAllLCs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LetterOfCredit> getLC(@PathVariable Long id) {
        return ResponseEntity.ok(letterOfCreditService.getLCById(id));
    }

    @PostMapping
    public ResponseEntity<LetterOfCredit> createLC(@RequestBody LetterOfCredit lc) {
        return ResponseEntity.ok(letterOfCreditService.createLC(lc));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LetterOfCredit> updateStatus(@PathVariable Long id, @RequestParam LetterOfCredit.LcStatus status) {
        return ResponseEntity.ok(letterOfCreditService.updateStatus(id, status));
    }
}
