package org.enterprise.crm.service.dto;

import lombok.Data;
import org.enterprise.crm.service.entity.ServiceRequest;

@Data
public class ServiceRequestDto {
    private Long id;
    private String requestNumber;
    private Long registeredProductId;
    private Long customerId;
    private String customerName;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantSku;
    private String issueDescription;
    private ServiceRequest.ServiceRequestPriority priority;
    private ServiceRequest.ServiceRequestStatus status;
    private String resolutionNotes;
    private ServiceRequest.ServiceLocationType serviceLocationType;
    private String serviceAddress;
    private Long assignedTechnicianId;
}
