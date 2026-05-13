package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.RegisteredProduct;
import org.enterprise.crm.service.repository.RegisteredProductRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegisteredProductService extends BaseService<RegisteredProduct, Long> {

    private final RegisteredProductRepository repository;

    public RegisteredProductService(RegisteredProductRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<RegisteredProduct> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId);
    }
}
