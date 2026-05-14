package org.enterprise.hr.repository;

import org.enterprise.hr.entity.PayslipComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PayslipComponentRepository 
    extends JpaRepository<PayslipComponent, Long>, JpaSpecificationExecutor<PayslipComponent> {
}
