package org.enterprise.reportengine.util;

public class SqlSecurityValidator {

    private SqlSecurityValidator() {
    }

    public static void validateSelectQuery(String sql) {

        String normalized = sql
                .trim()
                .toLowerCase();

        if (!normalized.startsWith("select")) {
            throw new RuntimeException("Only SELECT query allowed");
        }

        String[] blocked = {
                "drop ",
                "delete ",
                "truncate ",
                "insert ",
                "update ",
                "alter "
        };

        for (String word : blocked) {
            if (normalized.contains(word)) {
                throw new RuntimeException(
                        "Dangerous SQL keyword detected"
                );
            }
        }
    }
}
