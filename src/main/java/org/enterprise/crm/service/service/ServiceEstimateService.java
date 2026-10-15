package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.ServiceEstimate;
import org.enterprise.crm.service.entity.ServiceEstimateDetail;
import org.enterprise.crm.service.repository.ServiceEstimateRepository;
import org.enterprise.crm.service.repository.ServiceRequestRepository;
import org.enterprise.inventory.repository.ProductRepository;
import org.enterprise.inventory.repository.ProductVariantRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceEstimateService extends BaseService<ServiceEstimate, Long> {

    private final ServiceEstimateRepository repository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public ServiceEstimateService(
            ServiceEstimateRepository repository,
            ServiceRequestRepository serviceRequestRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository) {
        super(repository);
        this.repository = repository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public List<ServiceEstimate> findByServiceRequestId(Long serviceRequestId) {
        return repository.findByServiceRequestId(serviceRequestId);
    }

    @Transactional
    @Override
    public ServiceEstimate save(ServiceEstimate entity) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (companyId != null && entity.getCompanyId() == null) {
            entity.setCompanyId(companyId);
        }

        Long reqId = null;
        if (entity.getServiceRequest() != null && entity.getServiceRequest().getId() != null) {
            reqId = entity.getServiceRequest().getId();
        } else if (entity.getServiceRequestId() != null) {
            reqId = entity.getServiceRequestId();
        }

        if (reqId != null) {
            entity.setServiceRequest(serviceRequestRepository.findById(reqId).orElse(null));
        }

        if (entity.getDetails() != null) {
            for (ServiceEstimateDetail detail : entity.getDetails()) {
                detail.setServiceEstimate(entity);
                if (companyId != null && detail.getCompanyId() == null) {
                    detail.setCompanyId(companyId);
                }

                Long prodId = null;
                if (detail.getProduct() != null && detail.getProduct().getId() != null) {
                    prodId = detail.getProduct().getId();
                } else if (detail.getProductId() != null) {
                    prodId = detail.getProductId();
                }
                if (prodId != null) {
                    detail.setProduct(productRepository.findById(prodId).orElse(null));
                }

                Long varId = null;
                if (detail.getVariant() != null && detail.getVariant().getId() != null) {
                    varId = detail.getVariant().getId();
                } else if (detail.getVariantId() != null) {
                    varId = detail.getVariantId();
                }
                if (varId != null) {
                    detail.setVariant(productVariantRepository.findById(varId).orElse(null));
                }
            }
        }

        return super.save(entity);
    }

    public Page<ServiceEstimate> searchServiceEstimates(Long companyId, String searchTerm, Pageable pageable) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            return repository.searchServiceEstimates(companyId, searchTerm.trim(), pageable);
        }
        return repository.findAllByCompanyId(companyId, pageable);
    }

}