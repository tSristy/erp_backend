package org.enterprise.finance.controller;

import org.enterprise.finance.dto.AccountBalanceDTO;
import org.enterprise.finance.service.AccountBalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/account-balances")
public class AccountBalanceController {

    private final AccountBalanceService accountBalanceService;

    public AccountBalanceController(AccountBalanceService accountBalanceService) {
        this.accountBalanceService = accountBalanceService;
    }

    @GetMapping
    public ResponseEntity<List<AccountBalanceDTO>> getAll() {
        return ResponseEntity.ok(accountBalanceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountBalanceDTO> getById(@PathVariable Long id) {
        AccountBalanceDTO dto = accountBalanceService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<AccountBalanceDTO> create(@RequestBody AccountBalanceDTO dto) {
        return ResponseEntity.ok(accountBalanceService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountBalanceDTO> update(@PathVariable Long id, @RequestBody AccountBalanceDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(accountBalanceService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountBalanceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
