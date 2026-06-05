package org.enterprise.common.event;

import java.math.BigDecimal;

public interface PosPayment {
    String getPaymentModeName();
    BigDecimal getAmount();
    String getReferenceNumber();
}
