package com.restaurant.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection
 * 
 * Central utility class managing the JDBC connection to MySQL.
 * Configures UTF-8 and unicode connection parameters as required.
 * Checks for a local db.properties file or defaults to localhost credentials.
 * Provides startup verification and transparent SQL query logging to console.
 */
public class DBConnection {

    private static String dbUrl = "jdbc:mysql://localhost:3306/restaurant_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
    private static String dbUser = "root";
    private static String dbPassword = "pablo";
    private static String dbDriver = "com.mysql.cj.jdbc.Driver";

    static {
        loadConfiguration();
        try {
            Class.forName(dbDriver);
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    private static void loadConfiguration() {
        Properties props = new Properties();
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                props.load(fis);
            } catch (Exception ignored) {
            }
        } else {
            try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) {
                    props.load(is);
                }
            } catch (Exception ignored) {
            }
        }

        if (props.containsKey("db.url")) {
            dbUrl = props.getProperty("db.url");
        }
        if (props.containsKey("db.username")) {
            dbUser = props.getProperty("db.username");
        }
        if (props.containsKey("db.password")) {
            dbPassword = props.getProperty("db.password");
        }
        if (props.containsKey("db.driver")) {
            dbDriver = props.getProperty("db.driver");
        }
    }

    /**
     * Obtains a new active connection to the MySQL database.
     *
     * @return active java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Verifies database connectivity on application startup.
     * Throws an informative exception if connection fails.
     */
    public static void testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Connection was returned closed or null.");
            }
            System.out.println("[DBConnection] Successfully connected to MySQL at: " + dbUrl + " (User: " + dbUser + ")");
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection failed: " + e.getMessage());
            throw new SQLException(
                "Could not connect to MySQL database!\n\n" +
                "Connection URL: " + dbUrl + "\n" +
                "Username:       " + dbUser + "\n\n" +
                "Error details:  " + e.getMessage() + "\n\n" +
                "Troubleshooting steps:\n" +
                "1. Confirm MySQL Server is running (service 'MySQL' or 'MySQL267').\n" +
                "2. Verify database credentials in 'db.properties' in the application folder.\n" +
                "3. Verify that database 'restaurant_db' exists (run sql/schema.sql).",
                e
            );
        }
    }

    /**
     * Utility method to log SQL execution and returned row counts to console.
     */
    public static void logQuery(String context, String sql, int rowCount) {
        System.out.printf("[SQL LOG] [%s] %s --> Returned %d row(s)%n", context, sql, rowCount);
    }

    /**
     * Safely closes one or more closeable resources (Connection, Statement, ResultSet).
     */
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static void setCredentials(String url, String user, String password) {
        dbUrl = url;
        dbUser = user;
        dbPassword = password;
    }
}
