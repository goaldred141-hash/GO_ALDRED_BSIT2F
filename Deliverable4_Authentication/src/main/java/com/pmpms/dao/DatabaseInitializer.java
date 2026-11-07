package com.pmpms.dao;

import com.pmpms.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Creates every required table (if missing) before the CLI menu starts. */
public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {

            st.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id            INTEGER PRIMARY KEY AUTOINCREMENT,
                        username      TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL,
                        full_name     TEXT NOT NULL,
                        roles         TEXT NOT NULL,
                        status        TEXT NOT NULL DEFAULT 'PENDING',
                        created_at    DATETIME DEFAULT (datetime('now','localtime'))
                    )""");
            seedDefaultAdmin(conn);
            System.out.println("[DB] Database initialized (market.db).");
        } catch (SQLException e) {
            System.out.println("[DB] Failed to initialize database: " + e.getMessage());
            System.exit(1);
        }
    }

    /** First admin must exist, otherwise nobody could ever approve registrations. */
    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE roles = ?")) {
            check.setString(1, "ADMIN");
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        }
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO users (username, password_hash, full_name, roles, status) VALUES (?, ?, ?, ?, ?)")) {
            ins.setString(1, "admin");
            ins.setString(2, PasswordUtil.hash("admin123"));
            ins.setString(3, "Market Administrator");
            ins.setString(4, "ADMIN");
            ins.setString(5, "APPROVED");
            ins.executeUpdate();
            System.out.println("[DB] Default admin created -> username: admin | password: admin123");
        }
    }
}
