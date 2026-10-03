package org.enterprise.organization.service;

import org.enterprise.inventory.service.BaseService;
import org.enterprise.organization.entity.Zone;
import org.enterprise.organization.repository.ZoneRepository;
import org.enterprise.organization.specification.ZoneSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ZoneService extends BaseService<Zone, Long> {

    private final ZoneRepository repository;

    public ZoneService(ZoneRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Zone> search(String query, Pageable pageable) {
        return repository.findAll(ZoneSpecification.searchByQuery(query), pageable);
    }
}
