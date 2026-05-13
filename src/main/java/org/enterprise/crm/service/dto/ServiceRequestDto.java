package org.enterprise.crm.service.dto;

import lombok.Data;
import org.enterprise.crm.service.entity.ServiceRequest;

@Data
public class ServiceRequestDto {
    private Long id;
    private Long registeredProductId;
    private Long customerId;
    private String issueDescription;
    private ServiceRequest.ServiceRequestPriority priority;
    private ServiceRequest.ServiceRequestStatus status;
    private String resolutionNotes;
    private Long assignedTechnicianId;
}
