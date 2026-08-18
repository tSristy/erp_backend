package org.enterprise.inventory.service;

import org.enterprise.inventory.entity.UnitOfMeasure;
import org.enterprise.inventory.repository.UnitOfMeasureRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UnitOfMeasureService extends BaseService<UnitOfMeasure, Long> {

    private final UnitOfMeasureRepository repository;

    public UnitOfMeasureService(UnitOfMeasureRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<UnitOfMeasure> findByCompanyId(Long companyId) {
        return repository.findByCompanyId(companyId);
    }
}
