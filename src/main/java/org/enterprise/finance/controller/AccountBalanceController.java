package org.enterprise.finance.controller;

import org.enterprise.finance.dto.AccountBalanceDTO;
import org.enterprise.finance.service.AccountBalanceService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/account-balances")
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
    public ResponseEntity<AccountBalanceDTO> create(@Valid @RequestBody AccountBalanceDTO dto) {
        return ResponseEntity.ok(accountBalanceService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountBalanceDTO> update(@PathVariable Long id, @Valid @RequestBody AccountBalanceDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(accountBalanceService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountBalanceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.AccountBalance> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return accountBalanceService.searchAccountBalances(q, pageable);
    }
}
