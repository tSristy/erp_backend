package org.enterprise.finance.dto;

import lombok.Data;

@Data
public class AccountDTO {
    private Long id;
    private String code;
    private String name;
    private org.enterprise.finance.enums.AccountType accountType;
    private AccountDTO parent;
}
