package org.enterprise.hr.repository;

import org.enterprise.hr.entity.EmployeeLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeLoanRepository extends JpaRepository<EmployeeLoan, Long> {
}
