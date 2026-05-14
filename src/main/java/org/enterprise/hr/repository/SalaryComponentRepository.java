package org.enterprise.hr.repository;

import org.enterprise.hr.entity.SalaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SalaryComponentRepository 
    extends JpaRepository<SalaryComponent, Long>, JpaSpecificationExecutor<SalaryComponent> {
}
