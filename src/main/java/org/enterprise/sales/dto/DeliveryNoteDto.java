package org.enterprise.sales.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import org.enterprise.common.dto.AuditableDto;
import org.enterprise.sales.entity.DeliveryNote.DeliveryStatus;
import org.enterprise.sales.entity.DeliveryNote.DeliveryType;

@Data
public class DeliveryNoteDto extends AuditableDto {
    private String deliveryNo;
    private LocalDate deliveryDate;
    private DeliveryStatus status;
    private Long salesOrderId;
    private String salesOrderNo;
    private Long customerId;
    private String customerName;
    private Long warehouseId;
    private String warehouseName;
    private DeliveryType deliveryType;
    private org.enterprise.sales.entity.DeliveryNote.SalesChannel salesChannel;
    private List<DeliveryNoteDetailDto> details;
}
