package org.enterprise.hr.repository;

import org.enterprise.hr.entity.EmployeeRoster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRosterRepository extends JpaRepository<EmployeeRoster, Long> {
}
