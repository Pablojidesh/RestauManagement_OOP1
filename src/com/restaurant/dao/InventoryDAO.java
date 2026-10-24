package com.restaurant.dao;

import com.restaurant.config.DBConnection;
import com.restaurant.model.InventoryItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * InventoryDAO
 * 
 * Data Access Object for pantry raw materials and stock management.
 * Tracks stock levels, flags items reaching or breaching low-stock safety thresholds,
 * supports restocking operations, and computes total inventory holding valuation.
 */
public class InventoryDAO {

    /**
     * Inserts a new inventory item.
     * Validates that item name and unit are non-empty and numbers are non-negative.
     */
    public int addInventoryItem(InventoryItem item) {
        if (item == null) throw new IllegalArgumentException("Inventory item cannot be null.");
        if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty.");
        }
        if (item.getUnit() == null || item.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Unit of measurement cannot be empty.");
        }
        if (item.getQuantity() < 0 || item.getLowStockThreshold() < 0 || item.getCostPerUnit() < 0) {
            throw new IllegalArgumentException("Quantities, threshold, and cost must be non-negative.");
        }

        String sql = "INSERT INTO inventory (item_name, unit, quantity, low_stock_threshold, cost_per_unit) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getItemName().trim());
            stmt.setString(2, item.getUnit().trim());
            stmt.setDouble(3, item.getQuantity());
            stmt.setDouble(4, item.getLowStockThreshold());
            stmt.setDouble(5, item.getCostPerUnit());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        item.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[InventoryDAO] Error adding inventory item: " + e.getMessage());
            throw new RuntimeException("Database error adding inventory item: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Increases the quantity of an existing inventory item (Restock).
     *
     * @param id inventory item ID
     * @param additionalQuantity quantity to add (must be > 0)
     * @return true if updated successfully
     */
    public boolean restockItem(int id, double additionalQuantity) {
        if (id <= 0 || additionalQuantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be greater than 0.");
        }

        String sql = "UPDATE inventory SET quantity = quantity + ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, additionalQuantity);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[InventoryDAO] Error restocking item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all inventory items.
     * Skips invalid or corrupted rows.
     */
    public List<InventoryItem> getAllInventoryItems() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT id, item_name, unit, quantity, low_stock_threshold, cost_per_unit FROM inventory ORDER BY item_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("item_name");
                String unit = rs.getString("unit");
                double qty = rs.getDouble("quantity");
                double threshold = rs.getDouble("low_stock_threshold");
                double cost = rs.getDouble("cost_per_unit");

                if (name == null || name.trim().isEmpty() || unit == null || unit.trim().isEmpty()) {
                    continue; // Skip corrupted rows
                }

                list.add(new InventoryItem(id, name.trim(), unit.trim(), qty, threshold, cost));
            }
        } catch (SQLException e) {
            System.err.println("[InventoryDAO] Error fetching inventory items: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all items currently at or below their low-stock safety threshold.
     */
    public List<InventoryItem> getLowStockItems() {
        List<InventoryItem> lowStockList = new ArrayList<>();
        for (InventoryItem item : getAllInventoryItems()) {
            if (item.isLowStock()) {
                lowStockList.add(item);
            }
        }
        return lowStockList;
    }

    /**
     * Computes the total inventory cost = sum of (quantity * cost_per_unit) across all items.
     */
    public double getTotalInventoryCost() {
        String sql = "SELECT COALESCE(SUM(quantity * cost_per_unit), 0) FROM inventory";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("[InventoryDAO] Error calculating total inventory cost: " + e.getMessage());
        }
        return 0.0;
    }
}
