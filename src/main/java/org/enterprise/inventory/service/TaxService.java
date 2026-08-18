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
}
