package org.enterprise.crm.service.repository;

import org.enterprise.crm.service.entity.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
    List<ServiceOrder> findByServiceRequestId(Long serviceRequestId);
}
