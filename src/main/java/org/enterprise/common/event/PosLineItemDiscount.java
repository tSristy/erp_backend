package org.enterprise.common.event;

import java.math.BigDecimal;

public interface PosLineItemDiscount {
    String getDiscountName();
    BigDecimal getDiscountAmount();
}
