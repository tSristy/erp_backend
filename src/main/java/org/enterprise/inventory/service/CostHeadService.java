package org.enterprise.inventory.service;

import org.enterprise.inventory.entity.CostHead;
import org.enterprise.inventory.repository.CostHeadRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CostHeadService extends BaseService<CostHead, Long> {

    private final CostHeadRepository repository;

    public CostHeadService(CostHeadRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<CostHead> findByCompanyId(Long companyId) {
        return repository.findByCompanyId(companyId);
    }
}
