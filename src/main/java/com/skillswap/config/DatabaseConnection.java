package com.skillswap.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Reusable JDBC Database Connection Utility.
 * 
 * Securely loads database credentials from an external properties file (db.properties)
 * or system environment variables, ensuring no sensitive credentials are ever hardcoded.
 */
public class DatabaseConnection {

    private static final String DEFAULT_CONFIG_FILE = "db.properties";
    private static Properties properties = new Properties();
    private static boolean isLoaded = false;

    // Static block to initialize driver and load properties on class loading
    static {
        loadProperties();
    }

    private DatabaseConnection() {
        // Utility class private constructor
    }

    /**
     * Loads database credentials from classpath or project root.
     */
    private static synchronized void loadProperties() {
        if (isLoaded) {
            return;
        }

        // 1. Try loading from classpath (e.g., target/classes/db.properties or src/main/resources/db.properties)
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                isLoaded = true;
            }
        } catch (IOException e) {
            System.err.println("Warning: Unable to load " + DEFAULT_CONFIG_FILE + " from classpath: " + e.getMessage());
        }

        // 2. Fallback: try loading from working directory
        if (!isLoaded) {
            File localFile = new File(DEFAULT_CONFIG_FILE);
            if (localFile.exists()) {
                try (InputStream input = new FileInputStream(localFile)) {
                    properties.load(input);
                    isLoaded = true;
                } catch (IOException e) {
                    System.err.println("Warning: Unable to load " + DEFAULT_CONFIG_FILE + " from local directory: " + e.getMessage());
                }
            }
        }

        // 3. Fallback: check environment variables if file was not found
        if (!isLoaded) {
            String envUrl = System.getenv("DB_URL");
            String envUser = System.getenv("DB_USER");
            String envPassword = System.getenv("DB_PASSWORD");

            if (envUrl != null && envUser != null) {
                properties.setProperty("db.url", envUrl);
                properties.setProperty("db.user", envUser);
                if (envPassword != null) {
                    properties.setProperty("db.password", envPassword);
                }
                isLoaded = true;
            }
        }

        if (!isLoaded) {
            System.err.println("----------------------------------------------------------------------");
            System.err.println("CRITICAL CONFIGURATION NOTICE:");
            System.err.println("Could not find '" + DEFAULT_CONFIG_FILE + "' in classpath or working directory.");
            System.err.println("Please copy 'src/main/resources/db.properties.example' to 'db.properties'");
            System.err.println("and fill in your MySQL username and password.");
            System.err.println("----------------------------------------------------------------------");
        }
    }

    /**
     * Obtains a new database connection from the configured settings.
     * Callers must close the connection using try-with-resources.
     * 
     * @return Connection active JDBC connection to MySQL
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!isLoaded) {
            loadProperties();
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password", "");

        if (url == null || user == null) {
            throw new SQLException("Missing database connection settings. Please verify 'db.url' and 'db.user' in db.properties.");
        }

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            String errorMsg = """
                ======================================================================
                DATABASE CONNECTION FAILED!
                Error Code: %d, SQL State: %s
                Message: %s
                
                Troubleshooting Checklist:
                1. Ensure MySQL Server is running (Service name: MySQL80).
                2. Verify the username and password in 'db.properties'.
                3. Ensure the 'skillswap' database has been created using 'schema.sql'.
                4. Verify MySQL is listening on localhost port 3306.
                ======================================================================
                """.formatted(e.getErrorCode(), e.getSQLState(), e.getMessage());
            System.err.println(errorMsg);
            throw e;
        }
    }

    /**
     * Verifies the database connection and prints database metadata for diagnostics.
     * 
     * @return true if connection was successful, false otherwise
     */
    public static boolean testConnection() {
        System.out.println("Testing database connection to MySQL...");
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println(">>> Database Connection Successful! <<<");
                System.out.println(" - Database Product: " + metaData.getDatabaseProductName());
                System.out.println(" - Database Version: " + metaData.getDatabaseProductVersion());
                System.out.println(" - Driver Name:      " + metaData.getDriverName());
                System.out.println(" - Driver Version:   " + metaData.getDriverVersion());
                System.out.println(" - Current Catalog:  " + conn.getCatalog());
                return true;
            }
        } catch (SQLException e) {
            System.err.println(">>> Connection Test Failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Cleanly shuts down background driver threads (e.g. AbandonedConnectionCleanupThread)
     * so that console applications and Maven CLI exit immediately without lingering threads.
     */
    public static void shutdown() {
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Exception ignored) {
            // Safe to ignore on shutdown
        }
    }
}
