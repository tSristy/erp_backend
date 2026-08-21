package org.enterprise.production.repository;

import org.enterprise.production.entity.WorkCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkCenterRepository extends JpaRepository<WorkCenter, Long> {
    List<WorkCenter> findByCompanyId(Long companyId);
}
