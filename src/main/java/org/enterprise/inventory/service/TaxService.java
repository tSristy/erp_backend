package org.enterprise.inventory.service;

import org.enterprise.inventory.entity.Tax;
import org.enterprise.inventory.repository.TaxRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaxService extends BaseService<Tax, Long> {

    private final TaxRepository repository;

    public TaxService(TaxRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Tax> findByCompanyId(Long companyId) {
        return repository.findByCompanyId(companyId);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Tax> searchTaxs(String query, org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(org.enterprise.inventory.specification.TaxSpecification.searchByQuery(query), pageable);
    }
}
