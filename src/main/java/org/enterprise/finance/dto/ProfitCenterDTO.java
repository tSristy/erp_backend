package org.enterprise.finance.dto;

import lombok.Data;

@Data
public class ProfitCenterDTO {
    private Long id;
    private String code;
    private String name;
    private String shortName;
    private String type;
    private String managerName;
    private String managerEmail;
    private String contactNo;
    private String currencyCode;
    private java.time.LocalDate effectiveFrom;
    private java.time.LocalDate effectiveTo;
    private String remarks;
    private Long branchId;
    private ProfitCenterDTO parent;
    private java.util.List<ProfitCenterDTO> children;
}
