package org.enterprise.hr.repository;

import org.enterprise.hr.entity.MobileAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MobileAttendanceRepository extends JpaRepository<MobileAttendance, Long> {
}
