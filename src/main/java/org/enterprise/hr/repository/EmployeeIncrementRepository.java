package org.enterprise.hr.repository;

import org.enterprise.hr.entity.EmployeeIncrement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeIncrementRepository 
    extends JpaRepository<EmployeeIncrement, Long>, JpaSpecificationExecutor<EmployeeIncrement> {
}
