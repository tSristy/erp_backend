package org.enterprise.crm.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.crm.entity.LoyaltyProfile;
import org.enterprise.crm.repository.LoyaltyProfileRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crm/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyProfileRepository loyaltyProfileRepository;

    @GetMapping
    public ResponseEntity<List<LoyaltyProfile>> getAllProfiles() {
        return ResponseEntity.ok(loyaltyProfileRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoyaltyProfile> getProfileById(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return loyaltyProfileRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @org.springframework.web.bind.annotation.PostMapping
    public ResponseEntity<LoyaltyProfile> createProfile(@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody LoyaltyProfile profile) {
        LoyaltyProfile created = loyaltyProfileRepository.save(profile);
        java.net.URI location = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<LoyaltyProfile> updateProfile(@org.springframework.web.bind.annotation.PathVariable Long id, @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody LoyaltyProfile profile) {
        profile.setId(id);
        return ResponseEntity.ok(loyaltyProfileRepository.save(profile));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@org.springframework.web.bind.annotation.PathVariable Long id) {
        loyaltyProfileRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/search")
    public org.springframework.data.domain.Page<LoyaltyProfile> search(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return loyaltyProfileRepository.findAll(org.enterprise.crm.specification.LoyaltyProfileSpecification.searchByQuery(q), pageable);
    }
}
