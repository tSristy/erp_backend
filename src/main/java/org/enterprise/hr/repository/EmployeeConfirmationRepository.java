package org.enterprise.hr.repository;

import org.enterprise.hr.entity.EmployeeConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeConfirmationRepository 
    extends JpaRepository<EmployeeConfirmation, Long>, JpaSpecificationExecutor<EmployeeConfirmation> {
}
