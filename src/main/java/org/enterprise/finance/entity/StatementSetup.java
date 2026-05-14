package org.enterprise.finance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.finance.enums.BalanceType;
import org.enterprise.finance.enums.CalculationType;
import org.enterprise.finance.enums.ReportType;

import java.util.List;

@Entity
@Table(name = "fin_statement_setup", indexes = {
                @Index(name = "idx_stmt_report", columnList = "reportType"),
                @Index(name = "idx_stmt_serial", columnList = "serialNo")
})
@Getter
@Setter
public class StatementSetup extends AuditableEntity {

        private Integer serialNo;

        private Integer parentSerialNo;

        private Integer levelNo;

        @Enumerated(EnumType.STRING)
        private ReportType reportType;

        @Column(length = 500)
        private String particulars;

        @Enumerated(EnumType.STRING)
        private CalculationType calculationType;

        @Column(length = 1000)
        private String formula;

        private Boolean bold = false;

        private Boolean visible = true;

        private Boolean bottomLine = false;

        @Enumerated(EnumType.STRING)
        private BalanceType balanceType;

        @OneToMany(mappedBy = "statementSetup", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<StatementSetupAccount> accounts;
}
