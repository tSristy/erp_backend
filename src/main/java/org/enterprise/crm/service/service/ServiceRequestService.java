package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.RegisteredProduct;
import org.enterprise.crm.service.entity.ServiceRequest;
import org.enterprise.crm.service.repository.RegisteredProductRepository;
import org.enterprise.crm.service.repository.ServiceRequestRepository;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.ProductVariant;
import org.enterprise.inventory.repository.BusinessPartnerRepository;
import org.enterprise.inventory.repository.ProductRepository;
import org.enterprise.inventory.repository.ProductVariantRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceRequestService extends BaseService<ServiceRequest, Long> {

    private final ServiceRequestRepository repository;
    private final RegisteredProductRepository registeredProductRepository;
    private final BusinessPartnerRepository businessPartnerRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public ServiceRequestService(
            ServiceRequestRepository repository,
            RegisteredProductRepository registeredProductRepository,
            BusinessPartnerRepository businessPartnerRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository) {
        super(repository);
        this.repository = repository;
        this.registeredProductRepository = registeredProductRepository;
        this.businessPartnerRepository = businessPartnerRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public List<ServiceRequest> findByRegisteredProductId(Long registeredProductId) {
        return repository.findByRegisteredProductId(registeredProductId);
    }

    @Transactional
    @Override
    public ServiceRequest save(ServiceRequest entity) {
        if (entity.getRequestNumber() == null || entity.getRequestNumber().isEmpty()) {
            entity.setRequestNumber("SR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (companyId != null && entity.getCompanyId() == null) {
            entity.setCompanyId(companyId);
        }

        Long custId = null;
        if (entity.getCustomer() != null && entity.getCustomer().getId() != null) {
            custId = entity.getCustomer().getId();
        } else if (entity.getCustomerId() != null) {
            custId = entity.getCustomerId();
        }

        if (custId != null) {
            entity.setCustomer(businessPartnerRepository.findById(custId).orElse(null));
        } else {
            entity.setCustomer(null);
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
            if (product != null && (entity.getProductName() == null || entity.getProductName().isEmpty())) {
                entity.setProductName(product.getName());
            }
        } else {
            entity.setProduct(null);
        }

        Long varId = null;
        if (entity.getVariant() != null && entity.getVariant().getId() != null) {
            varId = entity.getVariant().getId();
        } else if (entity.getVariantId() != null) {
            varId = entity.getVariantId();
        }

        if (varId != null) {
            entity.setVariant(productVariantRepository.findById(varId).orElse(null));
        } else {
            entity.setVariant(null);
        }

        Long rpId = null;
        if (entity.getRegisteredProduct() != null && entity.getRegisteredProduct().getId() != null) {
            rpId = entity.getRegisteredProduct().getId();
        } else if (entity.getRegisteredProductId() != null) {
            rpId = entity.getRegisteredProductId();
        }

        if (rpId != null) {
            RegisteredProduct rp = registeredProductRepository.findById(rpId).orElse(null);
            entity.setRegisteredProduct(rp);
            if (rp != null) {
                if (entity.getCustomer() == null && rp.getCustomer() != null) {
                    entity.setCustomer(rp.getCustomer());
                }
                if (entity.getProduct() == null && rp.getProduct() != null) {
                    entity.setProduct(rp.getProduct());
                    if (entity.getProductName() == null || entity.getProductName().isEmpty()) {
                        entity.setProductName(rp.getProductName());
                    }
                }
                if (entity.getVariant() == null && rp.getVariant() != null) {
                    entity.setVariant(rp.getVariant());
                }
            }
        } else {
            entity.setRegisteredProduct(null);
        }

        return super.save(entity);
    }

    public org.springframework.data.domain.Page<ServiceRequest> searchServiceRequests(Long companyId, String searchTerm, org.springframework.data.domain.Pageable pageable) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            return repository.searchServiceRequests(companyId, searchTerm.trim(), pageable);
        }
        return repository.findAllByCompanyId(companyId, pageable);
    }

}