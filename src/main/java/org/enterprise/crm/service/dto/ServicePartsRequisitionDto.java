package org.enterprise.crm.service.dto;

import lombok.Data;
import org.enterprise.crm.service.entity.ServicePartsRequisition;

import java.util.List;

@Data
public class ServicePartsRequisitionDto {
    private Long id;
    private Long serviceRequestId;
    private Long requestedById;
    private Long warehouseId;
    private ServicePartsRequisition.RequisitionStatus status;
    private List<ServicePartsRequisitionDetailDto> details;
}
