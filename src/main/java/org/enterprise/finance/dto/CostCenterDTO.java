package org.enterprise.finance.dto;

import lombok.Data;

@Data
public class CostCenterDTO {
    private Long id;
    private String code;
    private String name;
    private String shortName;
    private org.enterprise.finance.enums.CostCenterType type;
    private String category;
    private String allocationMethod;
    private java.math.BigDecimal monthlyBudget;
    private java.math.BigDecimal yearlyBudget;
    private java.time.LocalDate effectiveFrom;
    private java.time.LocalDate effectiveTo;
    private String remarks;
    private Long branchId;
    private ProfitCenterDTO profitCenter;
    private CostCenterDTO parent;
    private java.util.List<CostCenterDTO> children;
}
