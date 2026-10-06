package org.enterprise.finance.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.finance.dto.FinancialStatementRowDto;
import org.enterprise.finance.entity.StatementSetup;
import org.enterprise.finance.entity.StatementSetupAccount;
import org.enterprise.finance.enums.CalculationType;
import org.enterprise.finance.enums.ReportType;
import org.enterprise.finance.repository.AccountBalanceRepository;
import org.enterprise.finance.repository.StatementSetupRepository;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FinancialStatementService {

    private final StatementSetupRepository setupRepository;

    private final AccountBalanceRepository balanceRepository;

    public List<FinancialStatementRowDto> generateStatement(
            ReportType reportType,
            Long periodId
    ) {

        List<StatementSetup> rows =
                setupRepository
                        .findByReportTypeOrderBySerialNo(
                                reportType
                        );

        Map<Integer, BigDecimal> calculated =
                new HashMap<>();

        List<FinancialStatementRowDto> result =
                new ArrayList<>();

        for (StatementSetup row : rows) {

            BigDecimal amount =
                    calculateRow(
                            row,
                            periodId,
                            calculated
                    );

            calculated.put(
                    row.getSerialNo(),
                    amount
            );

            result.add(
                    new FinancialStatementRowDto(
                            row.getSerialNo(),
                            row.getParentSerialNo(),
                            row.getLevelNo(),
                            row.getParticulars(),
                            amount,
                            row.getBold(),
                            row.getBottomLine(),
                            row.getVisible()
                    )
            );
        }

        return result;
    }

    private BigDecimal calculateRow(
            StatementSetup row,
            Long periodId,
            Map<Integer, BigDecimal> calculated
    ) {

        if (row.getCalculationType()
                == CalculationType.HEADER) {

            return BigDecimal.ZERO;
        }

        if (row.getCalculationType()
                == CalculationType.ACCOUNT_SUM) {

            return calculateAccountSum(
                    row,
                    periodId
            );
        }

        if (row.getCalculationType()
                == CalculationType.SUBTOTAL) {

            return calculateSubtotal(
                    row.getSerialNo(),
                    calculated
            );
        }

        if (row.getCalculationType()
                == CalculationType.FORMULA) {

            return evaluateFormula(
                    row.getFormula(),
                    calculated
            );
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calculateAccountSum(
            StatementSetup row,
            Long periodId
    ) {

        BigDecimal total = BigDecimal.ZERO;

        if (row.getAccounts() == null) {
            return total;
        }

        for (StatementSetupAccount mapping : row.getAccounts()) {
            BigDecimal balance = balanceRepository.getAccountBalance(
                    mapping.getAccount().getId(),
                    periodId
            );

            if (balance != null) {
                total = total.add(balance);
            }
        }

        if (row.getBalanceType() == org.enterprise.finance.enums.BalanceType.CREDIT) {
            return total.negate();
        }

        return total;
    }

    private BigDecimal calculateSubtotal(
            Integer serialNo,
            Map<Integer, BigDecimal> calculated
    ) {

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Integer, BigDecimal> e :
                calculated.entrySet()) {

            if (e.getKey() < serialNo) {
                total = total.add(e.getValue());
            }
        }

        return total;
    }

    private BigDecimal evaluateFormula(
            String formula,
            Map<Integer, BigDecimal> calculated
    ) {

        try {

            String expression = formula;

            for (Map.Entry<Integer, BigDecimal> e :
                    calculated.entrySet()) {

                expression = expression.replaceAll(
                        "\\b" + e.getKey() + "\\b",
                        e.getValue().toString()
                );
            }

            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(expression);
            Object result = exp.getValue();

            return new BigDecimal(
                    result.toString()
            );

        } catch (Exception e) {

            return BigDecimal.ZERO;
        }
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.StatementSetup> searchStatementSetups(String query, org.springframework.data.domain.Pageable pageable) {
        return setupRepository.findAll(org.enterprise.finance.specification.StatementSetupSpecification.searchByQuery(query), pageable);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.finance.entity.AccountBalance> searchAccountBalances(String query, org.springframework.data.domain.Pageable pageable) {
        return balanceRepository.findAll(org.enterprise.finance.specification.AccountBalanceSpecification.searchByQuery(query), pageable);
    }
}