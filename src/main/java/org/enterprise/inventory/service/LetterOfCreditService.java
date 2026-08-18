package org.enterprise.inventory.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.inventory.entity.LetterOfCredit;
import org.enterprise.inventory.repository.LetterOfCreditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LetterOfCreditService {

    private final LetterOfCreditRepository letterOfCreditRepository;

    public List<LetterOfCredit> getAllLCs() {
        Long companyId = TenantContext.getCompanyId();
        return letterOfCreditRepository.findByCompanyId(companyId);
    }

    public LetterOfCredit getLCById(Long id) {
        return letterOfCreditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Letter of Credit not found"));
    }

    @Transactional
    public LetterOfCredit createLC(LetterOfCredit lc) {
        Long companyId = TenantContext.getCompanyId();
        lc.setCompanyId(companyId);
        return letterOfCreditRepository.save(lc);
    }

    @Transactional
    public LetterOfCredit updateStatus(Long id, LetterOfCredit.LcStatus status) {
        LetterOfCredit lc = getLCById(id);
        lc.setStatus(status);
        return letterOfCreditRepository.save(lc);
    }
}
