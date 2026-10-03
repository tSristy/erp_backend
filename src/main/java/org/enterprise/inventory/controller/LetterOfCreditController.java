package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.LetterOfCredit;
import org.enterprise.inventory.service.LetterOfCreditService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/letters-of-credit")
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
    public ResponseEntity<LetterOfCredit> createLC(@Valid @RequestBody LetterOfCredit lc) {
        return ResponseEntity.ok(letterOfCreditService.createLC(lc));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LetterOfCredit> updateStatus(@PathVariable Long id, @RequestParam LetterOfCredit.LcStatus status) {
        return ResponseEntity.ok(letterOfCreditService.updateStatus(id, status));
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.LetterOfCredit> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return letterOfCreditService.searchLetterOfCredits(q, pageable);
    }
}
