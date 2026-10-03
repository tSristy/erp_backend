package org.enterprise.inventory.service;

import org.enterprise.inventory.entity.Category;
import org.enterprise.inventory.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService extends BaseService<Category, Long> {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Category> findByCompanyId(Long companyId) {
        return repository.findByCompanyId(companyId);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Category> searchCategorys(String query, org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(org.enterprise.inventory.specification.CategorySpecification.searchByQuery(query), pageable);
    }
}
