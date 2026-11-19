package database;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Subject: Problem Solving Using OOP (Java)
 * Topic: Enterprise JDBC Database Connection Factory
 * 
 * Supports:
 * 1. Self-contained SQLite File Database (clash_of_bytes.db) - Default zero-config mode
 * 2. External PostgreSQL or MySQL Database if specified in db.properties or Environment
 */
public class DBConnection {

    private static final String DEFAULT_SQLITE_FILE = "clash_of_bytes.db";

    private static String resolveSqliteFilePath() {
        File direct = new File("clash_of_bytes.db");
        if (direct.exists()) return "clash_of_bytes.db";
        File sub = new File("Clash_Of_Bytes/clash_of_bytes.db");
        if (sub.exists()) return sub.getPath();
        File parent = new File("../clash_of_bytes.db");
        if (parent.exists()) return parent.getPath();
        return "clash_of_bytes.db";
    }

    private static String dbUrl = "jdbc:sqlite:" + resolveSqliteFilePath();
    private static String dbUser = "";
    private static String dbPassword = "";
    private static String dbDriver = "org.sqlite.JDBC";

    private static Connection connection = null;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                Properties props = new Properties();
                props.load(fis);
                dbUrl = props.getProperty("db.url", dbUrl);
                dbUser = props.getProperty("db.user", dbUser);
                dbPassword = props.getProperty("db.password", dbPassword);
                dbDriver = props.getProperty("db.driver", dbDriver);
            } catch (Exception e) {
                System.err.println("[DBConnection] Warning: Error reading db.properties, falling back to default SQLite: " + e.getMessage());
            }
        }
    }

    /**
     * Get or create a thread-safe active Connection.
     */
    public static synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                try {
                    Class.forName(dbDriver);
                } catch (ClassNotFoundException e) {
                    // Fallback to SQLite if primary driver missing
                    System.err.println("[DBConnection] Driver not found: " + dbDriver + ". Attempting SQLite fallback...");
                    Class.forName("org.sqlite.JDBC");
                    dbDriver = "org.sqlite.JDBC";
                    dbUrl = "jdbc:sqlite:" + DEFAULT_SQLITE_FILE;
                }

                if (dbUser != null && !dbUser.trim().isEmpty()) {
                    connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                } else {
                    connection = DriverManager.getConnection(dbUrl);
                }

                // Enable Foreign Key support in SQLite
                if (dbUrl.contains("sqlite")) {
                    try (java.sql.Statement stmt = connection.createStatement()) {
                        stmt.execute("PRAGMA foreign_keys = ON;");
                    }
                }
            }
            return connection;
        } catch (ClassNotFoundException ex) {
            throw new SQLException("Database Driver class not found: " + dbDriver, ex);
        }
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {}
            connection = null;
        }
    }
}
