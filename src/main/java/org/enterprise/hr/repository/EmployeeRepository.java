package org.enterprise.hr.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.enterprise.hr.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Page<Employee> findByEmployeeCodeContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String employeeCode, String fullName, Pageable pageable);

    boolean existsByEmployeeCode(String employeeCode);
}