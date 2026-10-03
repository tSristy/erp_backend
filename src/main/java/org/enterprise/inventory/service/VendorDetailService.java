package org.enterprise.inventory.service;

import org.enterprise.inventory.entity.VendorDetail;
import org.enterprise.inventory.repository.VendorDetailRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VendorDetailService extends BaseService<VendorDetail, Long> {

    private final VendorDetailRepository repository;

    public VendorDetailService(VendorDetailRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<VendorDetail> findByCompanyId(Long companyId) {
        return repository.findByCompanyId(companyId);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.VendorDetail> searchVendorDetails(String query, org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(org.enterprise.inventory.specification.VendorDetailSpecification.searchByQuery(query), pageable);
    }
}
