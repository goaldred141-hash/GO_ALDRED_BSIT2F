package com.market.dao;

import com.market.model.Commodity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** SQL for the commodities table. */
public class CommodityDao {

    private final ActivityLogDao logDao;

    public CommodityDao(ActivityLogDao logDao) {
        this.logDao = logDao;
    }

    /** Adds a commodity and writes a COMMODITY_ADDED audit log. */
    public boolean addCommodity(String name, String category, String unit, int userId) {
        String sql = "INSERT INTO commodities (name, category, unit) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setString(3, unit);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                logDao.logAction(userId, "COMMODITY_ADDED",
                        "Added commodity " + name + " (" + category + ", per " + unit + ")");
            }
            return ok;
        } catch (SQLException e) {
            System.out.println("Could not add commodity (is the name already used?): " + e.getMessage());
            return false;
        }
    }

    public List<Commodity> getActiveCommodities() {
        String sql = "SELECT id, name, category, unit FROM commodities WHERE status = ? ORDER BY id";
        List<Commodity> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "ACTIVE");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Commodity(rs.getInt("id"), rs.getString("name"),
                            rs.getString("category"), rs.getString("unit")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error while loading commodities: " + e.getMessage());
        }
        return list;
    }
}
