package org.enterprise.sales.dto;

import lombok.Data;
import java.math.BigDecimal;
import org.enterprise.common.dto.AuditableDto;

@Data
public class DeliveryNoteDetailDto extends AuditableDto {
    private Long deliveryNoteId;
    private Long salesOrderDetailId;
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private Long batchId;
    private java.util.List<String> serialNumbers;
}
