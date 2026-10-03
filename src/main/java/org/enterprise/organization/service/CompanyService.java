package org.enterprise.organization.service;

import org.enterprise.inventory.service.BaseService;
import org.enterprise.organization.entity.Company;
import org.enterprise.organization.repository.CompanyRepository;
import org.enterprise.organization.specification.CompanySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService extends BaseService<Company, Long> {

    private final CompanyRepository repository;

    public CompanyService(CompanyRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Company> search(String query, Pageable pageable) {
        return repository.findAll(CompanySpecification.searchByQuery(query), pageable);
    }
}
