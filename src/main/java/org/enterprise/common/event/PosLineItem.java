package org.enterprise.common.event;

import java.math.BigDecimal;
import java.util.List;

public interface PosLineItem {
    Long getProductId();
    BigDecimal getQuantity();
    BigDecimal getUnitPrice();
    BigDecimal getLineTotal();
    List<? extends PosLineItemDiscount> getLineDiscounts();

    default Long getVariantId() {
        return null;
    }

    default Long getBatchId() {
        return null;
    }

    default String getBatchNo() {
        return null;
    }

    default String getSerialNo() {
        return null;
    }

    default List<String> getSerialNumbers() {
        return List.of();
    }
}
