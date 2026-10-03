package org.enterprise.inventory.service;

import org.enterprise.inventory.entity.Attribute;
import org.enterprise.inventory.repository.AttributeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AttributeService extends BaseService<Attribute, Long> {

    private final AttributeRepository repository;

    public AttributeService(AttributeRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Attribute> findByCompanyId(Long companyId) {
        return repository.findByCompanyId(companyId);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Attribute> searchAttributes(String query, org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(org.enterprise.inventory.specification.AttributeSpecification.searchByQuery(query), pageable);
    }
}
