package org.enterprise.crm.service.repository;

import org.enterprise.crm.service.entity.ServicePartsRequisition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePartsRequisitionRepository extends JpaRepository<ServicePartsRequisition, Long> {
    List<ServicePartsRequisition> findByServiceRequestId(Long serviceRequestId);
}
