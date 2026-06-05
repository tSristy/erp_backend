package org.enterprise.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ManufacturingOrderCompletedEvent extends ApplicationEvent {

    private final String orderNo;
    private final Long finishedGoodId;
    private final Long productionWarehouseId;
    private final BigDecimal producedQuantity;
    private final Long bomId;
    private final LocalDateTime completionDate;

    public ManufacturingOrderCompletedEvent(Object source, 
                                            String orderNo, 
                                            Long finishedGoodId, 
                                            Long productionWarehouseId, 
                                            BigDecimal producedQuantity, 
                                            Long bomId,
                                            LocalDateTime completionDate) {
        super(source);
        this.orderNo = orderNo;
        this.finishedGoodId = finishedGoodId;
        this.productionWarehouseId = productionWarehouseId;
        this.producedQuantity = producedQuantity;
        this.bomId = bomId;
        this.completionDate = completionDate;
    }
}
