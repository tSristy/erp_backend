package org.enterprise.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgingReportLineDto {
    private Long partnerId;
    private String partnerName;
    
    private BigDecimal currentBalance = BigDecimal.ZERO;
    private BigDecimal days30 = BigDecimal.ZERO;
    private BigDecimal days60 = BigDecimal.ZERO;
    private BigDecimal days90 = BigDecimal.ZERO;
    private BigDecimal daysOver90 = BigDecimal.ZERO;
    
    private BigDecimal totalBalance = BigDecimal.ZERO;
}
