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

    @GetMapping
    public ResponseEntity<List<RetailTransaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RetailTransaction> getTransaction(@PathVariable Long id) {
        return transactionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RetailTransaction> createTransaction(@RequestBody RetailTransaction transaction) {
        // Here we could just save it as PENDING or immediately complete it.
        // For POS, typically the submission is the completion of the sale.
        transactionService.completeTransaction(transaction);
        return ResponseEntity.ok(transaction);
    }
}
