package com.pmpms.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Single place that opens SQLite connections. Foreign keys are enforced on EVERY connection. */
public final class DatabaseConnection {

    /** SQLite database file, created in the project root on first run. */
    private static final String URL = "jdbc:sqlite:market.db";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }
}
