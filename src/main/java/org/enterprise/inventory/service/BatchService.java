package org.enterprise.inventory.service;

import org.enterprise.inventory.entity.Batch;
import org.enterprise.inventory.repository.BatchRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BatchService extends BaseService<Batch, Long> {

    private final BatchRepository repository;

    public BatchService(BatchRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Batch> findByCompanyId(Long companyId) {
        return repository.findByCompanyId(companyId);
    }
}
