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


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Batch> searchBatchs(String query, org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(org.enterprise.inventory.specification.BatchSpecification.searchByQuery(query), pageable);
    }
}
