package org.enterprise.hr.repository;

import org.enterprise.hr.entity.EmployeePromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeePromotionRepository 
    extends JpaRepository<EmployeePromotion, Long>, JpaSpecificationExecutor<EmployeePromotion> {
}
