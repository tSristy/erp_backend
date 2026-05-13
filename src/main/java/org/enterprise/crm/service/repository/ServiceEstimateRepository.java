package org.enterprise.crm.service.repository;

import org.enterprise.crm.service.entity.ServiceEstimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceEstimateRepository extends JpaRepository<ServiceEstimate, Long> {
    List<ServiceEstimate> findByServiceRequestId(Long serviceRequestId);
}
