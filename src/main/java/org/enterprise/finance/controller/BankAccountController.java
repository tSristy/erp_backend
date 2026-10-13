package org.enterprise.finance.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.enterprise.finance.entity.BankAccount;
import org.enterprise.finance.service.BankAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @GetMapping
    public ResponseEntity<List<BankAccount>> getAllBankAccounts() {
        return ResponseEntity.ok(bankAccountService.getAllBankAccounts());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BankAccount>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        String searchQuery = (query != null && !query.trim().isEmpty()) ? query : q;
        return ResponseEntity.ok(bankAccountService.searchBankAccounts(searchQuery, pageable));
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<BankAccount> getBankAccount(@PathVariable Long id) {
        return ResponseEntity.ok(bankAccountService.getBankAccountById(id));
    }

    @PostMapping
    public ResponseEntity<BankAccount> createBankAccount(@Valid @RequestBody BankAccount bankAccount) {
        return ResponseEntity.ok(bankAccountService.createBankAccount(bankAccount));
    }

    @PutMapping("/{id:[0-9]+}")
    public ResponseEntity<BankAccount> updateBankAccount(@PathVariable Long id, @Valid @RequestBody BankAccount bankAccount) {
        return ResponseEntity.ok(bankAccountService.updateBankAccount(id, bankAccount));
    }

    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Void> deleteBankAccount(@PathVariable Long id) {
        bankAccountService.deleteBankAccount(id);
        return ResponseEntity.noContent().build();
    }
}
