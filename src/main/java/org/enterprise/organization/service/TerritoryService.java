package org.enterprise.organization.service;

import org.enterprise.inventory.service.BaseService;
import org.enterprise.organization.entity.Territory;
import org.enterprise.organization.repository.TerritoryRepository;
import org.enterprise.organization.specification.TerritorySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TerritoryService extends BaseService<Territory, Long> {

    private final TerritoryRepository repository;

    public TerritoryService(TerritoryRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Territory> search(String query, Pageable pageable) {
        return repository.findAll(TerritorySpecification.searchByQuery(query), pageable);
    }
}
