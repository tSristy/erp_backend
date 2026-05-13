package org.enterprise.crm.service.repository;

import org.enterprise.crm.service.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByRegisteredProductId(Long registeredProductId);
    List<ServiceRequest> findByCustomerId(Long customerId);
}
