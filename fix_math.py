path = 'src/main/java/org/enterprise/finance/service/FinancialStatementService.java'
with open(path, 'r') as f:
    content = f.read()

import re
old_block = r"""    private BigDecimal calculateAccountSum\(
            StatementSetup row,
            Long periodId
    \) \{

        BigDecimal total = BigDecimal\.ZERO;

        if \(row\.getAccounts\(\) == null\) \{
            return total;
        \}

        for \(StatementSetupAccount mapping :
                row\.getAccounts\(\)\) \{

            BigDecimal balance =
                    balanceRepository\.getAccountBalance\(
                            mapping\.getAccount\(\)\.getId\(\),
                            periodId
                    \);

            total = total\.add\(balance\);
        \}

        return total;
    \}"""

new_block = """    private BigDecimal calculateAccountSum(
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
    }"""

content = re.sub(old_block, new_block, content)

with open(path, 'w') as f:
    f.write(content)
