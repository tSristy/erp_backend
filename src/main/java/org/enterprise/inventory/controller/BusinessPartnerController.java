package org.enterprise.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.service.BusinessPartnerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business-partners")
@RequiredArgsConstructor
public class BusinessPartnerController {

    private final BusinessPartnerService service;

    @PostMapping
    public BusinessPartner create(@RequestBody BusinessPartner bp) {
        return service.save(bp);
    }

    @GetMapping
    public List<BusinessPartner> getAll() {
        return service.findAll();
    }

    @GetMapping("/code/{code}")
    public BusinessPartner getByCode(@PathVariable String code,
                                     @RequestParam Long companyId) {
        return service.findByCode(code, companyId)
                .orElseThrow(() -> new RuntimeException("Business Partner not found"));
    }
}