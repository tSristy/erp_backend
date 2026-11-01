package org.enterprise.crm.sales.controller;

import org.enterprise.crm.sales.entity.Lead;
import org.enterprise.crm.sales.service.LeadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crm/sales/leads")
public class LeadController {

    private final LeadService service;

    public LeadController(LeadService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Lead>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lead> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Lead> create(@RequestBody Lead lead) {
        return ResponseEntity.ok(service.save(lead));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lead> update(@PathVariable Long id, @RequestBody Lead lead) {
        return service.findById(id).map(existing -> {
            lead.setId(existing.getId());
            if (lead.getConvertedCustomerId() == null) {
                lead.setConvertedCustomerId(existing.getConvertedCustomerId());
            }
            if (lead.getConvertedOpportunityId() == null) {
                lead.setConvertedOpportunityId(existing.getConvertedOpportunityId());
            }
            return ResponseEntity.ok(service.save(lead));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<Lead> convert(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, Object> body) {
        String opportunityName = body != null && body.get("opportunityName") != null ? String.valueOf(body.get("opportunityName")) : null;
        java.math.BigDecimal amount = null;
        if (body != null && body.get("amount") != null && !String.valueOf(body.get("amount")).trim().isEmpty()) {
            try {
                amount = new java.math.BigDecimal(String.valueOf(body.get("amount")).trim());
            } catch (Exception ignored) {}
        }
        java.time.LocalDate closeDate = null;
        if (body != null && body.get("expectedCloseDate") != null && !String.valueOf(body.get("expectedCloseDate")).trim().isEmpty()) {
            try {
                closeDate = java.time.LocalDate.parse(String.valueOf(body.get("expectedCloseDate")).trim());
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(service.convertLead(id, opportunityName, amount, closeDate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<Lead> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long companyId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.searchLeads(companyId, q, pageable);
    }

}