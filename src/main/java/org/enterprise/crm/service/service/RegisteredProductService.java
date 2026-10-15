package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.RegisteredProduct;
import org.enterprise.crm.service.repository.RegisteredProductRepository;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.ProductVariant;
import org.enterprise.inventory.service.BaseService;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.repository.ProductRepository;
import org.enterprise.inventory.repository.BusinessPartnerRepository;
import org.enterprise.inventory.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegisteredProductService extends BaseService<RegisteredProduct, Long> {

    private final RegisteredProductRepository repository;
    private final ProductRepository productRepository;
    private final BusinessPartnerRepository businessPartnerRepository;
    private final ProductVariantRepository productVariantRepository;

    public RegisteredProductService(
            RegisteredProductRepository repository, 
            ProductRepository productRepository,
            BusinessPartnerRepository businessPartnerRepository,
            ProductVariantRepository productVariantRepository) {
        super(repository);
        this.repository = repository;
        this.productRepository = productRepository;
        this.businessPartnerRepository = businessPartnerRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public List<RegisteredProduct> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Transactional
    @Override
    public RegisteredProduct save(RegisteredProduct entity) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (companyId != null && entity.getCompanyId() == null) {
            entity.setCompanyId(companyId);
        }

        Long prodId = null;
        if (entity.getProduct() != null && entity.getProduct().getId() != null) {
            prodId = entity.getProduct().getId();
        } else if (entity.getProductId() != null) {
            prodId = entity.getProductId();
        }

        if (prodId != null) {
            Product product = productRepository.findById(prodId).orElse(null);
            entity.setProduct(product);
            if (product != null) {
                if (entity.getProductName() == null || entity.getProductName().isEmpty()) {
                    entity.setProductName(product.getName());
                }
                if (entity.getBrand() == null || entity.getBrand().isEmpty()) {
                    entity.setBrand(product.getBrand() != null ? product.getBrand().getName() : null);
                }
            }
        } else {
            entity.setProduct(null);
        }

        Long custId = null;
        if (entity.getCustomer() != null && entity.getCustomer().getId() != null) {
            custId = entity.getCustomer().getId();
        } else if (entity.getCustomerId() != null) {
            custId = entity.getCustomerId();
        }

        if (custId != null) {
            BusinessPartner customer = businessPartnerRepository.findById(custId).orElse(null);
            entity.setCustomer(customer);
        } else {
            entity.setCustomer(null);
        }

        Long varId = null;
        if (entity.getVariant() != null && entity.getVariant().getId() != null) {
            varId = entity.getVariant().getId();
        } else if (entity.getVariantId() != null) {
            varId = entity.getVariantId();
        }

        if (varId != null) {
            ProductVariant pv = productVariantRepository.findById(varId).orElse(null);
            entity.setVariant(pv);
        } else {
            entity.setVariant(null);
        }

        return super.save(entity);
    }

    public org.springframework.data.domain.Page<RegisteredProduct> searchRegisteredProducts(Long companyId, String searchTerm, org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(pageable);
    }

}