package com.pmpms.dao;

import com.pmpms.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** All SQL that touches the users table lives here. Every query is a PreparedStatement. */
public class UserDao {

    public UserDao() {
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("username"), rs.getString("full_name"),
                rs.getString("roles"), rs.getString("status"));
    }

    // ------------------------------------------------------------------ Deliverable 4

    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return true; // fail safe: pretend it exists so we don't create a duplicate
        }
    }

    /** Inserts a new account with status PENDING. Returns the new id, or -1 on failure. */
    public int registerUser(String username, String passwordHash, String fullName, String role) {
        String sql = "INSERT INTO users (username, password_hash, full_name, roles, status) VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            ps.setString(4, role.toUpperCase());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return -1;
        }
    }

    /** Returns the user if username + password hash match (any status). */
    public Optional<User> authenticate(String username, String passwordHash) {
        String sql = "SELECT id, username, full_name, roles, status FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return Optional.empty();
        }
    }

    // ------------------------------------------------------------------ Deliverable 5

    /** All registered accounts regardless of role. */
    public List<User> getAllUsers() {
        return queryUsers("SELECT id, username, full_name, roles, status FROM users ORDER BY id", null);
    }

    /** Accounts awaiting approval, across all roles. */
    public List<User> getPendingUsers() {
        return queryUsers("SELECT id, username, full_name, roles, status FROM users WHERE status = ? ORDER BY id", "PENDING");
    }

    private List<User> queryUsers(String sql, String param) {
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return list;
    }

    /** PENDING -> APPROVED. */
    public boolean approveUser(int userId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }
}
