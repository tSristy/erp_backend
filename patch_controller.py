path = 'src/main/java/org/enterprise/finance/controller/StatementSetupController.java'
with open(path, 'r') as f:
    content = f.read()

import_statement = "import org.springframework.jdbc.core.JdbcTemplate;\nimport jakarta.annotation.PostConstruct;\n"
if "JdbcTemplate" not in content:
    content = content.replace('import java.net.URI;', 'import java.net.URI;\n' + import_statement)

bean_statement = """
    private final JdbcTemplate jdbcTemplate;

    public StatementSetupController(StatementSetupService statementSetupService, JdbcTemplate jdbcTemplate) {
        this.statementSetupService = statementSetupService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("ALTER TABLE fin_statement_setup DROP CONSTRAINT IF EXISTS fin_statement_setup_balance_type_check;");
            System.out.println("====== DROPPED BALANCE_TYPE CHECK CONSTRAINT ======");
        } catch(Exception e) {
            System.out.println("====== CONSTRAINT ALREADY DROPPED OR NOT FOUND ======");
        }
    }
"""

import re
content = re.sub(r'public StatementSetupController\(StatementSetupService statementSetupService\) \{[^}]+\}', bean_statement, content)

with open(path, 'w') as f:
    f.write(content)
