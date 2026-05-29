package org.enterprise.hr.repository;

import org.enterprise.hr.entity.BiometricDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BiometricDeviceRepository extends JpaRepository<BiometricDevice, Long> {
}
