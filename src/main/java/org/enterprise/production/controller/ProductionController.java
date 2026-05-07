package org.enterprise.production.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.production.service.ProductionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/production-orders")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;

    @PostMapping("/{id}/complete")
    public ResponseEntity<String> completeProduction(
            @PathVariable Long id,
            @RequestParam BigDecimal producedQty) {
        
        productionService.completeProduction(id, producedQty);
        return ResponseEntity.ok("Production completed successfully for quantity: " + producedQty);
    }
}
