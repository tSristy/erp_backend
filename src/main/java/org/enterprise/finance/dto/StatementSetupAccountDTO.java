package org.enterprise.finance.dto;

import lombok.Data;

@Data
public class StatementSetupAccountDTO {
    private Long id;
    private StatementSetupDTO statementSetup;
    private AccountDTO account;
}
