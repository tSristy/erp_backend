package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.RegisteredProduct;
import org.enterprise.crm.service.repository.RegisteredProductRepository;
import org.enterprise.inventory.service.BaseService;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegisteredProductService extends BaseService<RegisteredProduct, Long> {

    private final RegisteredProductRepository repository;
    private final ProductRepository productRepository;

    public RegisteredProductService(RegisteredProductRepository repository, ProductRepository productRepository) {
        super(repository);
        this.repository = repository;
        this.productRepository = productRepository;
    }

    public List<RegisteredProduct> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Override
    public RegisteredProduct save(RegisteredProduct entity) {
        if (entity.getProduct() != null && entity.getProduct().getId() != null) {
            if ((entity.getProductName() == null || entity.getProductName().isEmpty()) ||
                (entity.getBrand() == null || entity.getBrand().isEmpty())) {
                
                Product product = productRepository.findById(entity.getProduct().getId()).orElse(null);
                if (product != null) {
                    if (entity.getProductName() == null || entity.getProductName().isEmpty()) entity.setProductName(product.getName());
                    if (entity.getBrand() == null || entity.getBrand().isEmpty()) entity.setBrand(product.getBrand() != null ? product.getBrand().getName() : null);
                }
            }
        }
        return super.save(entity);
    }
}
