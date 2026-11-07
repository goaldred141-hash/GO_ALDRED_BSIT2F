package com.market.dao;

import com.market.model.User;
import com.market.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** All SQL that touches the users table lives here. */
public class UserDao {

    private final ActivityLogDao logDao;

    public UserDao(ActivityLogDao logDao) {
        this.logDao = logDao;
    }

    /** Creates a new account. Every new account starts as PENDING. */
    public boolean registerUser(String username, String password, String fullName, String role) {
        String sql = "INSERT INTO users (username, password_hash, full_name, roles, status) "
                + "VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ps.setString(3, fullName);
            ps.setString(4, role.toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Database error while registering: " + e.getMessage());
            return false;
        }
    }

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
            return false;
        }
    }

    /**
     * Checks the credentials only. Returns the matching user (any status) or null if the
     * username/password is wrong. The caller decides what to do with PENDING accounts.
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT id, username, full_name, roles, status FROM users "
                + "WHERE username = ? AND password_hash = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error while logging in: " + e.getMessage());
        }
        return null;
    }

    /** Returns every registered account, regardless of role or status. */
    public List<User> getAllUsers() {
        String sql = "SELECT id, username, full_name, roles, status FROM users ORDER BY id";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            System.out.println("Database error while loading users: " + e.getMessage());
        }
        return users;
    }

    /** Returns every account (all roles) whose status is PENDING. */
    public List<User> getPendingUsers() {
        String sql = "SELECT id, username, full_name, roles, status FROM users WHERE status = ? ORDER BY id";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "PENDING");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error while loading pending users: " + e.getMessage());
        }
        return users;
    }

    /** Changes an account from PENDING to APPROVED. */
    public boolean approveUser(int userId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Database error while approving user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Changes a user's role and status in one statement.
     * Values are converted to UPPER CASE before they reach the database.
     */
    public boolean updateUserRoleAndStatus(int userId, String newRole, String newStatus) {
        String sql = "UPDATE users SET roles = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newRole.toUpperCase());
            ps.setString(2, newStatus.toUpperCase());
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Database error while updating user: " + e.getMessage());
            return false;
        }
    }

    /** Approves the account AND records a USER_APPROVED audit log. */
    public boolean approveUser(int userId, int adminId) {
        boolean ok = approveUser(userId);
        if (ok) {
            logDao.logAction(adminId, "USER_APPROVED",
                    "Admin [" + adminId + "] approved account for User ID " + userId);
        }
        return ok;
    }

    /** Updates role/status AND records a USER_UPDATED audit log. */
    public boolean updateUserRoleAndStatus(int userId, String newRole, String newStatus, int adminId) {
        boolean ok = updateUserRoleAndStatus(userId, newRole, newStatus);
        if (ok) {
            logDao.logAction(adminId, "USER_UPDATED", "Admin [" + adminId + "] changed User " + userId
                    + " to role " + newRole.toUpperCase() + ", status " + newStatus.toUpperCase());
        }
        return ok;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("roles"),
                rs.getString("status"));
    }
}
