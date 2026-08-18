package org.enterprise.pos.retail.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.pos.retail.entity.RetailTransaction;
import org.enterprise.pos.retail.repository.RetailTransactionRepository;
import org.enterprise.pos.retail.service.RetailTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos/retail/transactions")
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
    public ResponseEntity<org.enterprise.pos.retail.dto.RetailTransactionDto> createTransaction(@RequestBody org.enterprise.pos.retail.dto.RetailTransactionDto dto) {
        RetailTransaction transaction = mapper.toEntity(dto);
        transactionService.completeTransaction(transaction);
        return ResponseEntity.ok(mapper.toDto(transaction));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<org.enterprise.pos.retail.dto.RetailTransactionDto> updateTransaction(@PathVariable Long id, @RequestBody org.enterprise.pos.retail.dto.RetailTransactionDto dto) {
        dto.setId(id);
        RetailTransaction transaction = mapper.toEntity(dto);
        return ResponseEntity.ok(mapper.toDto(transactionService.save(transaction)));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.ok().build();
    }
}
