package com.market.dao;

import com.market.model.Vendor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** SQL for the vendors table. */
public class VendorDao {

    private final ActivityLogDao logDao;

    public VendorDao(ActivityLogDao logDao) {
        this.logDao = logDao;
    }

    /** Registers a vendor/stall and writes a VENDOR_ADDED audit log. */
    public boolean addVendor(String stallNo, String vendorName, String contactNo, int userId) {
        String sql = "INSERT INTO vendors (stall_no, vendor_name, contact_no) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stallNo);
            ps.setString(2, vendorName);
            ps.setString(3, contactNo);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                logDao.logAction(userId, "VENDOR_ADDED",
                        "Registered vendor " + vendorName + " at stall " + stallNo);
            }
            return ok;
        } catch (SQLException e) {
            System.out.println("Could not register vendor (is the stall number already used?): " + e.getMessage());
            return false;
        }
    }

    public List<Vendor> getActiveVendors() {
        String sql = "SELECT id, stall_no, vendor_name, contact_no FROM vendors WHERE status = ? ORDER BY id";
        List<Vendor> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "ACTIVE");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Vendor(rs.getInt("id"), rs.getString("stall_no"),
                            rs.getString("vendor_name"), rs.getString("contact_no")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error while loading vendors: " + e.getMessage());
        }
        return list;
    }
}
