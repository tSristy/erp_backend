package org.enterprise.crm.service.repository;

import org.enterprise.crm.service.entity.MaintenanceSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {
    List<MaintenanceSchedule> findByRegisteredProductId(Long registeredProductId);

    @Query("SELECT m FROM MaintenanceSchedule m LEFT JOIN m.registeredProduct rp LEFT JOIN rp.product p WHERE " +
            "(:searchTerm IS NULL OR :searchTerm = '' OR " +
            "LOWER(m.maintenanceType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(m.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(rp.serialNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(rp.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<MaintenanceSchedule> searchMaintenanceSchedules(
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
}
