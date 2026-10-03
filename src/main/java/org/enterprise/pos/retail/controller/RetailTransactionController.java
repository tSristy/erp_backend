package org.enterprise.pos.retail.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.pos.retail.entity.RetailTransaction;
import org.enterprise.pos.retail.repository.RetailTransactionRepository;
import org.enterprise.pos.retail.service.RetailTransactionService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pos/retail/transactions")
@RequiredArgsConstructor
public class RetailTransactionController {

    private final RetailTransactionService transactionService;
    private final RetailTransactionRepository transactionRepository;
    private final org.enterprise.pos.retail.mapper.PosRetailMapper mapper;

    @GetMapping
    public ResponseEntity<List<org.enterprise.pos.retail.dto.RetailTransactionDto>> getAllTransactions() {
        return ResponseEntity.ok(mapper.toDtoListRetailTransaction(transactionService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<org.enterprise.pos.retail.dto.RetailTransactionDto> getTransaction(@PathVariable Long id) {
        return transactionService.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<org.enterprise.pos.retail.dto.RetailTransactionDto> createTransaction(@Valid @RequestBody org.enterprise.pos.retail.dto.RetailTransactionDto dto) {
        RetailTransaction transaction = mapper.toEntity(dto);
        transactionService.completeTransaction(transaction);
        org.enterprise.pos.retail.dto.RetailTransactionDto created = mapper.toDto(transaction);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<org.enterprise.pos.retail.dto.RetailTransactionDto> updateTransaction(@PathVariable Long id, @Valid @RequestBody org.enterprise.pos.retail.dto.RetailTransactionDto dto) {
        dto.setId(id);
        RetailTransaction transaction = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(transactionService.save(transaction)));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<RetailTransaction> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return transactionService.searchRetailTransactions(org.enterprise.common.util.TenantContext.getCompanyId(), q, pageable);
    }

}