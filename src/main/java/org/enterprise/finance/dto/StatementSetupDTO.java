package org.enterprise.finance.dto;

import lombok.Data;

@Data
public class StatementSetupDTO {
    private Long id;
    private Integer serialNo;
    private Integer parentSerialNo;
    private Integer levelNo;
    private org.enterprise.finance.enums.ReportType reportType;
    private String particulars;
    private org.enterprise.finance.enums.CalculationType calculationType;
    private String formula;
    private org.enterprise.finance.enums.BalanceType balanceType;
    private java.util.List<StatementSetupAccountDTO> accounts;
}
