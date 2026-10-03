package org.enterprise.inventory.service;

import org.enterprise.inventory.entity.Brand;
import org.enterprise.inventory.repository.BrandRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BrandService extends BaseService<Brand, Long> {

    private final BrandRepository repository;

    public BrandService(BrandRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Brand> findByCompanyId(Long companyId) {
        return repository.findByCompanyId(companyId);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Brand> searchBrands(String query, org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(org.enterprise.inventory.specification.BrandSpecification.searchByQuery(query), pageable);
    }
}
