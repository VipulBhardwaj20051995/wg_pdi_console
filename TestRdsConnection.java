import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class TestRdsConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://dev.c9ic2i82ui7u.us-east-1.rds.amazonaws.com:3306/wovengold_pdi";
        Properties props = new Properties();
        props.setProperty("user", "admin");
        props.setProperty("password", "Vipul9821128392");
        props.setProperty("connectTimeout", "5000");
        props.setProperty("socketTimeout", "5000");
        props.setProperty("useSSL", "false");
        props.setProperty("allowPublicKeyRetrieval", "true");
        
        System.out.println("Attempting to connect to MySQL database at:");
        System.out.println(url);
        
        try {
            // Load the driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded successfully");
            
            // Connect to the database
            System.out.println("Connecting to database...");
            Connection conn = DriverManager.getConnection(url, props);
            
            if (conn != null) {
                System.out.println("SUCCESS: Database connection established!");
                System.out.println("Connection valid: " + !conn.isClosed());
                conn.close();
                System.out.println("Connection closed");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: MySQL JDBC Driver not found");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("ERROR: Could not connect to the database");
            System.err.println("SQLException: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("VendorError: " + e.getErrorCode());
        }
    }
} 