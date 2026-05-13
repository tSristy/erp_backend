package org.enterprise.crm.service.dto;

import lombok.Data;
import org.enterprise.crm.service.entity.MaintenanceSchedule;

import java.time.LocalDate;

@Data
public class MaintenanceScheduleDto {
    private Long id;
    private Long registeredProductId;
    private LocalDate scheduledDate;
    private String maintenanceType;
    private MaintenanceSchedule.MaintenanceStatus status;
    private String notes;
}
