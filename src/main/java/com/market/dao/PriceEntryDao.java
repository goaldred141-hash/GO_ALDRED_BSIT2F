package com.market.dao;

import com.market.model.PriceEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL for the price_entries table.
 * Core business process : addPriceEntry()          -> PRICE_ENCODED
 * Lifecycle / state change: updateValidationStatus() -> PRICE_STATUS_EDIT
 */
public class PriceEntryDao {

    private static final String SELECT_JOINED =
            "SELECT p.id, c.name AS commodity_name, v.stall_no, v.vendor_name, p.price, "
                    + "p.date_recorded, p.validation_status "
                    + "FROM price_entries p "
                    + "JOIN commodities c ON c.id = p.commodity_id "
                    + "JOIN vendors v ON v.id = p.vendor_id ";

    private final ActivityLogDao logDao;

    public PriceEntryDao(ActivityLogDao logDao) {
        this.logDao = logDao;
    }

    /** Encodes a daily price (status starts as PENDING) and logs it. */
    public boolean addPriceEntry(int commodityId, int vendorId, double price, int recordedBy) {
        String sql = "INSERT INTO price_entries (commodity_id, vendor_id, price, recorded_by) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, commodityId);
            ps.setInt(2, vendorId);
            ps.setDouble(3, price);
            ps.setInt(4, recordedBy);
            if (ps.executeUpdate() == 0) {
                return false;
            }
            int newId = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    newId = keys.getInt(1);
                }
            }
            logDao.logAction(recordedBy, "PRICE_ENCODED", String.format(
                    "Encoded price P%.2f for Commodity ID %d at Vendor ID %d (Price Entry #%d)",
                    price, commodityId, vendorId, newId));
            return true;
        } catch (SQLException e) {
            System.out.println("Could not save the price entry: " + e.getMessage());
            return false;
        }
    }

    public List<PriceEntry> getAllPriceEntries() {
        return query(SELECT_JOINED + "ORDER BY p.id", null);
    }

    public List<PriceEntry> getPriceEntriesByStatus(String status) {
        return query(SELECT_JOINED + "WHERE p.validation_status = ? ORDER BY p.id", status);
    }

    /** Changes the validation status (PENDING -> VALIDATED / REJECTED) and logs old and new status. */
    public boolean updateValidationStatus(int priceId, String newStatus, int adminId) {
        String selectSql = "SELECT validation_status FROM price_entries WHERE id = ?";
        String updateSql = "UPDATE price_entries SET validation_status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            String oldStatus;
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, priceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    oldStatus = rs.getString("validation_status");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, newStatus.toUpperCase());
                ps.setInt(2, priceId);
                if (ps.executeUpdate() == 0) {
                    return false;
                }
            }
            logDao.logAction(adminId, "PRICE_STATUS_EDIT", "Updated Price Entry #" + priceId
                    + " status from " + oldStatus + " to " + newStatus.toUpperCase());
            return true;
        } catch (SQLException e) {
            System.out.println("Could not update the price entry: " + e.getMessage());
            return false;
        }
    }

    private List<PriceEntry> query(String sql, String statusFilter) {
        List<PriceEntry> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (statusFilter != null) {
                ps.setString(1, statusFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PriceEntry(
                            rs.getInt("id"),
                            rs.getString("commodity_name"),
                            rs.getString("stall_no"),
                            rs.getString("vendor_name"),
                            rs.getDouble("price"),
                            rs.getString("date_recorded"),
                            rs.getString("validation_status")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error while loading price entries: " + e.getMessage());
        }
        return list;
    }
}
