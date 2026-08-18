package org.enterprise.inventory.service;

import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends BaseService<Product, Long> {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public Page<Product> searchProducts(Long companyId, String searchTerm, Pageable pageable) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            return repository.findByCompanyIdAndNameContainingIgnoreCaseOrCompanyIdAndSkuContainingIgnoreCase(
                    companyId, searchTerm, companyId, searchTerm, pageable);
        }
        return repository.findByCompanyId(companyId, pageable);
    }
}
