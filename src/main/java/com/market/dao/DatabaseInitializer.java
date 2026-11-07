package com.market.dao;

import com.market.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/** Makes sure every required table exists BEFORE the CLI menu starts. */
public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id            INTEGER PRIMARY KEY AUTOINCREMENT,
                        username      TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL,
                        full_name     TEXT NOT NULL,
                        roles         TEXT NOT NULL CHECK (roles IN ('ADMIN','OFFICER','VIEWER')),
                        status        TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('APPROVED','PENDING')),
                        created_at    TEXT NOT NULL DEFAULT (datetime('now','localtime'))
                    )""");

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS commodities (
                        id         INTEGER PRIMARY KEY AUTOINCREMENT,
                        name       TEXT NOT NULL UNIQUE,
                        category   TEXT NOT NULL,
                        unit       TEXT NOT NULL,
                        status     TEXT NOT NULL DEFAULT 'ACTIVE',
                        created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))
                    )""");

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS vendors (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        stall_no    TEXT NOT NULL UNIQUE,
                        vendor_name TEXT NOT NULL,
                        contact_no  TEXT,
                        status      TEXT NOT NULL DEFAULT 'ACTIVE'
                    )""");

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS price_entries (
                        id                INTEGER PRIMARY KEY AUTOINCREMENT,
                        commodity_id      INTEGER NOT NULL,
                        vendor_id         INTEGER NOT NULL,
                        price             REAL NOT NULL CHECK (price > 0),
                        date_recorded     TEXT NOT NULL DEFAULT (date('now','localtime')),
                        recorded_by       INTEGER NOT NULL,
                        validation_status TEXT NOT NULL DEFAULT 'PENDING',
                        FOREIGN KEY (commodity_id) REFERENCES commodities(id),
                        FOREIGN KEY (vendor_id)    REFERENCES vendors(id),
                        FOREIGN KEY (recorded_by)  REFERENCES users(id)
                    )""");

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS activity_logs (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id     INTEGER NOT NULL,
                        action_type TEXT NOT NULL,
                        description TEXT NOT NULL,
                        timestamp   TEXT NOT NULL DEFAULT (datetime('now','localtime')),
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    )""");
        }
        seedDefaultAdmin();
    }

    /**
     * Every new account starts as PENDING, so someone must be able to approve the first ones.
     * A default administrator (admin / admin123) is created once if it does not exist yet.
     */
    private static void seedDefaultAdmin() throws SQLException {
        String sql = "INSERT OR IGNORE INTO users (username, password_hash, full_name, roles, status) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "admin");
            ps.setString(2, PasswordUtil.hash("admin123"));
            ps.setString(3, "System Administrator");
            ps.setString(4, "ADMIN");
            ps.setString(5, "APPROVED");
            ps.executeUpdate();
        }
    }
}
