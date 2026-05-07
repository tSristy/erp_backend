package org.enterprise.workflow.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WorkflowRuleEngine {

    public boolean evaluate(
            BigDecimal amount,
            String operator,
            String value
    ) {

        BigDecimal compare = new BigDecimal(value);

        return switch (operator) {
            case ">" -> amount.compareTo(compare) > 0;
            case "<" -> amount.compareTo(compare) < 0;
            case ">=" -> amount.compareTo(compare) >= 0;
            case "<=" -> amount.compareTo(compare) <= 0;
            case "==" -> amount.compareTo(compare) == 0;
            default -> false;
        };
    }
}