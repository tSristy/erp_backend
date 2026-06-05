package org.enterprise.common.event;

import java.math.BigDecimal;
import java.util.List;

public interface PosLineItem {
    Long getProductId();
    BigDecimal getQuantity();
    BigDecimal getUnitPrice();
    BigDecimal getLineTotal();
    List<? extends PosLineItemDiscount> getLineDiscounts();
}
