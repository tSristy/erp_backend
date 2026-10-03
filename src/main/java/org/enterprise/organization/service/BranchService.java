package org.enterprise.organization.service;

import org.enterprise.inventory.service.BaseService;
import org.enterprise.organization.entity.Branch;
import org.enterprise.organization.repository.BranchRepository;
import org.enterprise.organization.specification.BranchSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BranchService extends BaseService<Branch, Long> {

    private final BranchRepository repository;

    public BranchService(BranchRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Branch> search(String query, Pageable pageable) {
        return repository.findAll(BranchSpecification.searchByQuery(query), pageable);
    }
}
