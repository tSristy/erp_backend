package org.enterprise.hr.repository;

import org.enterprise.hr.entity.BiometricRawLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BiometricRawLogRepository extends JpaRepository<BiometricRawLog, Long> {
}
