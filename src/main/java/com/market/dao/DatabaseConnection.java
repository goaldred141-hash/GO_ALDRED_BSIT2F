package com.market.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Single place that opens SQLite connections.
 * The database is the file students.db in the project's working directory
 * (the project root when you run from IntelliJ). Open that same file in
 * SQLite Studio / DB Browser to verify your data.
 * Foreign key enforcement is OFF by default in SQLite, so it must be switched on
 * for EVERY new connection.
 */
public class DatabaseConnection {

    private static final String DB_FILE = "students.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found. Reload Maven so the sqlite-jdbc dependency is downloaded.", e);
        }
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /** Full path of the SQLite file, so you know exactly which file to open in DB Browser. */
    public static String getDatabasePath() {
        return new File(DB_FILE).getAbsolutePath();
    }
}
