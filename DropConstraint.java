import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropConstraint {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/enterpriseDB";
        String user = "postgres";
        String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "0p3n";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate("ALTER TABLE fin_statement_setup DROP CONSTRAINT IF EXISTS fin_statement_setup_balance_type_check;");
            System.out.println("Constraint dropped successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
