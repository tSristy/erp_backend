package org.enterprise.organization.service;

import org.enterprise.inventory.service.BaseService;
import org.enterprise.organization.entity.Area;
import org.enterprise.organization.repository.AreaRepository;
import org.enterprise.organization.specification.AreaSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AreaService extends BaseService<Area, Long> {

    private final AreaRepository repository;

    public AreaService(AreaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Area> search(String query, Pageable pageable) {
        return repository.findAll(AreaSpecification.searchByQuery(query), pageable);
    }
}
