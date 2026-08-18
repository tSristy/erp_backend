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
}
