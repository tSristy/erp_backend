package org.enterprise.crm.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.crm.entity.LoyaltyProfile;
import org.enterprise.crm.repository.LoyaltyProfileRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crm/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyProfileRepository loyaltyProfileRepository;

    @GetMapping("/profiles")
    public List<LoyaltyProfile> getAllProfiles() {
        return loyaltyProfileRepository.findAll();
    }
}
