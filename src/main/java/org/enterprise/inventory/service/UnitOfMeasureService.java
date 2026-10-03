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


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.UnitOfMeasure> searchUnitOfMeasures(String query, org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(org.enterprise.inventory.specification.UnitOfMeasureSpecification.searchByQuery(query), pageable);
    }
}
